#!/bin/bash

# k8s 배포 스크립트
# 사용법: ./deploy-k8s.sh <module-name> [image-tag]
# 예시: ./deploy-k8s.sh baro-auth latest

set -e

# 스크립트 디렉토리 기준으로 k8s 디렉토리 경로 찾기
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR=""

# 스크립트가 scripts/ 디렉토리에 있다고 가정
if [ -d "$SCRIPT_DIR/../cloud" ]; then
  K8S_DIR="$SCRIPT_DIR/.."
elif [ -d "$SCRIPT_DIR/../../k8s/cloud" ]; then
  K8S_DIR="$SCRIPT_DIR/../../k8s"
elif [ -d "/home/ubuntu/apps/k8s/cloud" ]; then
  K8S_DIR="/home/ubuntu/apps/k8s"
else
  echo "❌ k8s 디렉토리를 찾을 수 없습니다."
  exit 1
fi

MODULE_NAME=${1:-}
IMAGE_TAG=${2:-latest}

if [ -z "$MODULE_NAME" ]; then
  echo "❌ 모듈 이름이 필요합니다."
  echo "사용법: $0 <module-name> [image-tag]"
  exit 1
fi

echo "🚀 k8s 배포 시작: $MODULE_NAME (이미지 태그: $IMAGE_TAG)"

# kubectl 명령어 확인 (일반 kubectl 또는 sudo k3s kubectl)
KUBECTL_CMD=""

# 1. kubectl이 있는지 확인하고 실제로 동작하는지 테스트
if command -v kubectl &> /dev/null; then
  # kubectl이 실제로 클러스터에 접근할 수 있는지 테스트
  if kubectl get nodes &> /dev/null 2>&1; then
    KUBECTL_CMD="kubectl"
    echo "✅ 일반 kubectl 사용 가능 (클러스터 접근 성공)"
  elif command -v k3s &> /dev/null; then
    # kubectl이 있지만 클러스터에 접근 실패, sudo k3s kubectl 시도
    if sudo k3s kubectl get nodes &> /dev/null 2>&1; then
      KUBECTL_CMD="sudo k3s kubectl"
      echo "✅ sudo k3s kubectl 사용 (일반 kubectl은 permission 문제)"
    fi
  fi
fi

# 2. kubectl이 없거나 동작하지 않으면 sudo k3s kubectl 시도
if [ -z "$KUBECTL_CMD" ] && command -v k3s &> /dev/null; then
  if sudo k3s kubectl get nodes &> /dev/null 2>&1; then
    KUBECTL_CMD="sudo k3s kubectl"
    echo "✅ sudo k3s kubectl 사용"
  fi
fi

# 3. 최종 확인
if [ -z "$KUBECTL_CMD" ]; then
  echo "❌ kubectl 또는 k3s가 설치되어 있지 않거나 클러스터에 연결할 수 없습니다."
  echo "디버깅 정보:"
  if command -v kubectl &> /dev/null; then
    echo "kubectl get nodes 결과:"
    kubectl get nodes 2>&1 || true
  fi
  if command -v k3s &> /dev/null; then
    echo "sudo k3s kubectl get nodes 결과:"
    sudo k3s kubectl get nodes 2>&1 || true
  fi
  exit 1
fi

echo "📦 사용할 kubectl 명령어: $KUBECTL_CMD"

# EC2 Private IP 확인 (여러 방법 시도)
EC2_IP=""
# 방법 1: EC2 메타데이터 서비스
EC2_IP=$(curl -s --max-time 2 http://169.254.169.254/latest/meta-data/local-ipv4 2>/dev/null || echo "")
# 방법 2: hostname -I 사용
if [ -z "$EC2_IP" ]; then
  EC2_IP=$(hostname -I | awk '{print $1}' 2>/dev/null || echo "")
fi
# 방법 3: ip 명령어 사용
if [ -z "$EC2_IP" ]; then
  EC2_IP=$(ip route get 8.8.8.8 2>/dev/null | awk '{print $7; exit}' || echo "")
fi

if [ -z "$EC2_IP" ]; then
  echo "⚠️  EC2 Private IP를 자동으로 감지할 수 없습니다."
  echo "💡 hostNetwork를 사용하므로 127.0.0.1을 사용합니다."
  EC2_IP="127.0.0.1"
fi

echo "📍 EC2 Private IP: $EC2_IP"

# 모듈별 배포 경로 결정
case "$MODULE_NAME" in
  cloud)
    # cloud 모듈 전체 배포 (eureka -> config -> gateway 순서)
    echo "📦 Cloud 모듈 전체 배포 시작..."
    
    # 이미지 태그 업데이트 함수
    update_image_tag() {
      local DEPLOYMENT_FILE="$1"
      local SERVICE_NAME="$2"
      if [ -f "$DEPLOYMENT_FILE" ]; then
        CURRENT_IMAGE=$(grep -E "^\s+image:" "$DEPLOYMENT_FILE" | head -1 | awk '{print $2}')
        NEW_IMAGE="ghcr.io/do-develop-space/${SERVICE_NAME}:${IMAGE_TAG}"
        if [ "$CURRENT_IMAGE" != "$NEW_IMAGE" ]; then
          sed -i.bak "s|ghcr.io/do-develop-space/${SERVICE_NAME}:[^[:space:]]*|${NEW_IMAGE}|g" "$DEPLOYMENT_FILE"
          rm -f "${DEPLOYMENT_FILE}.bak" 2>/dev/null || true
        fi
      fi
    }
    
    echo "1️⃣ Eureka 배포 중..."
    update_image_tag "$K8S_DIR/cloud/eureka/deployment.yaml" "eureka"
    $KUBECTL_CMD apply -f "$K8S_DIR/cloud/eureka/"
    # latest 태그인 경우에만 재시작
    if grep -q ":latest" "$K8S_DIR/cloud/eureka/deployment.yaml" 2>/dev/null; then
      $KUBECTL_CMD rollout restart deployment/eureka -n baro-prod || true
    fi
    $KUBECTL_CMD wait --for=condition=ready pod -l app=eureka -n baro-prod --timeout=300s || true
    
    echo "2️⃣ Config 배포 중..."
    update_image_tag "$K8S_DIR/cloud/config/deployment.yaml" "config"
    $KUBECTL_CMD apply -f "$K8S_DIR/cloud/config/"
    
    # [Config Server 동적 브랜치 설정]
    # 현재 배포 브랜치에 맞춰 Config Server가 해당 브랜치의 설정 파일을 보도록 설정
    # 예: main-auth 배포 시 Config Server도 main-auth 브랜치의 설정 파일 사용
    #
    # ====================================================================
    # 활성화 방법:
    # ====================================================================
    # 1. .github/workflows/ci-cd.yml의 "Deploy to k8s" 단계에서
    #    GIT_BRANCH 환경 변수 주석을 해제:
    #    GIT_BRANCH: ${{ github.ref_name }}  # 배포 브랜치 이름
    #
    # 2. 아래 주석을 해제하여 Config Server 환경 변수 업데이트 활성화
    #
    # ====================================================================
    # 동작 예시:
    # ====================================================================
    # - main-auth 브랜치 배포 시:
    #   → Config Server가 main-auth 브랜치의 application.yml 파일 제공
    #   → baro-auth 서비스가 main-auth 브랜치의 설정을 사용
    #
    # - main-cloud 브랜치 배포 시:
    #   → Config Server가 main-cloud 브랜치의 application.yml 파일 제공
    #   → cloud 모듈들이 main-cloud 브랜치의 설정을 사용
    #
    # ====================================================================
    # 주의사항:
    # ====================================================================
    # 1. Config Server를 여러 모듈이 공유하는 경우:
    #    - 마지막에 배포된 브랜치의 설정을 모든 서비스가 사용하게 됨
    #    - 예: main-auth 배포 후 main-cloud 배포 시, 모든 서비스가 main-cloud 설정 사용
    #    - 해결: 모듈별로 별도의 Config Server 인스턴스 운영 고려
    #
    # 2. 모든 모듈이 같은 설정을 봐야 하는 경우:
    #    - 이 기능을 비활성화하고 기본값(main)을 사용하는 것이 안전
    #
    # 3. 브랜치 전략:
    #    - feature → main-{module}로 직접 머지하는 경우 이 기능 유용
    #    - feature → main → main-{module}인 경우 main 브랜치 유지 권장
    #
    # ====================================================================
    # 코드 (주석 해제하여 활성화):
    # ====================================================================
    # if [ -n "$GIT_BRANCH" ]; then
    #   echo "🔧 Config Server 브랜치 업데이트: $GIT_BRANCH"
    #   $KUBECTL_CMD set env deployment/config CONFIG_GIT_BRANCH="$GIT_BRANCH" -n baro-prod
    #   echo "✅ Config Server가 $GIT_BRANCH 브랜치를 보도록 설정됨"
    #   # 환경 변수 변경 후 Config Server 재시작 필요
    #   $KUBECTL_CMD rollout restart deployment/config -n baro-prod || true
    # else
    #   echo "ℹ️  GIT_BRANCH 환경 변수가 없어 Config Server는 기본값(main) 브랜치를 사용합니다."
    # fi
    #
    # 수동 실행 시 (CI/CD 없이):
    # GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "main")
    # $KUBECTL_CMD set env deployment/config CONFIG_GIT_BRANCH="$GIT_BRANCH" -n baro-prod
    # $KUBECTL_CMD rollout restart deployment/config -n baro-prod
    
    if grep -q ":latest" "$K8S_DIR/cloud/config/deployment.yaml" 2>/dev/null; then
      $KUBECTL_CMD rollout restart deployment/config -n baro-prod || true
    fi
    $KUBECTL_CMD wait --for=condition=ready pod -l app=config -n baro-prod --timeout=300s || true
    
    echo "3️⃣ Gateway 배포 중..."
    update_image_tag "$K8S_DIR/cloud/gateway/deployment.yaml" "gateway"
    $KUBECTL_CMD apply -f "$K8S_DIR/cloud/gateway/"
    if grep -q ":latest" "$K8S_DIR/cloud/gateway/deployment.yaml" 2>/dev/null; then
      $KUBECTL_CMD rollout restart deployment/gateway -n baro-prod || true
    fi
    $KUBECTL_CMD wait --for=condition=ready pod -l app=gateway -n baro-prod --timeout=300s || true
    
    echo "✅ Cloud 모듈 배포 완료"
    $KUBECTL_CMD get pods -n baro-prod -l component=cloud
    exit 0
    ;;
  eureka|config|gateway)
    DEPLOY_PATH="$K8S_DIR/cloud/$MODULE_NAME"
    ;;
  redis)
    DEPLOY_PATH="$K8S_DIR/redis"
    ;;
  auth)
    DEPLOY_PATH="$K8S_DIR/apps/baro-auth"
    ;;
  buyer)
    DEPLOY_PATH="$K8S_DIR/apps/baro-buyer"
    ;;
  seller)
    DEPLOY_PATH="$K8S_DIR/apps/baro-seller"
    ;;
  order)
    DEPLOY_PATH="$K8S_DIR/apps/baro-order"
    ;;
  support)
    DEPLOY_PATH="$K8S_DIR/apps/baro-support"
    ;;
  *)
    echo "❌ 알 수 없는 모듈: $MODULE_NAME"
    exit 1
    ;;
esac

# 배포 경로 확인
if [ ! -d "$DEPLOY_PATH" ]; then
  echo "❌ 배포 경로를 찾을 수 없습니다: $DEPLOY_PATH"
  exit 1
fi

# Deployment 파일에 EC2 IP 설정 (애플리케이션 모듈인 경우, Redis는 제외)
# 주의: hostNetwork를 사용하므로 실제로는 127.0.0.1을 사용하지만, 
# 기존 코드와의 호환성을 위해 EC2_IP 변수는 유지
if [[ "$MODULE_NAME" =~ ^(auth|buyer|seller|order|support)$ ]]; then
  DEPLOYMENT_FILE="$DEPLOY_PATH/deployment.yaml"
  if [ -f "$DEPLOYMENT_FILE" ]; then
    # hostNetwork를 사용하므로 EC2 IP 설정은 필요 없음
    # 하지만 기존 코드 호환성을 위해 주석 처리
    # echo "🔧 EC2 IP 설정 중: $DEPLOYMENT_FILE"
    # sed -i.bak "s/CHANGE_ME_TO_EC2_IP/$EC2_IP/g" "$DEPLOYMENT_FILE"
    # rm -f "${DEPLOYMENT_FILE}.bak" 2>/dev/null || true
    echo "ℹ️  hostNetwork 사용으로 EC2 IP 설정 불필요 (127.0.0.1 사용)"
  fi
fi

# 이미지 태그 업데이트 (Deployment 파일에서)
DEPLOYMENT_FILE="$DEPLOY_PATH/deployment.yaml"
if [ -f "$DEPLOYMENT_FILE" ]; then
  # 현재 Deployment에 설정된 이미지 태그 확인
  CURRENT_IMAGE=$(grep -E "^\s+image:" "$DEPLOYMENT_FILE" | head -1 | awk '{print $2}')
  SERVICE_NAME=$(echo "$CURRENT_IMAGE" | sed -E 's|.*/([^:]+):.*|\1|')
  
  # 새로운 이미지 태그 구성
  NEW_IMAGE="ghcr.io/do-develop-space/${SERVICE_NAME}:${IMAGE_TAG}"
  
  # 태그가 변경되었는지 확인
  if [ "$CURRENT_IMAGE" != "$NEW_IMAGE" ]; then
    echo "🏷️  이미지 태그 업데이트: $CURRENT_IMAGE -> $NEW_IMAGE"
    # 모든 이미지 태그 패턴 교체 (latest, main-*, dev-* 등 모든 태그 지원)
    sed -i.bak "s|ghcr.io/do-develop-space/${SERVICE_NAME}:[^[:space:]]*|${NEW_IMAGE}|g" "$DEPLOYMENT_FILE"
    rm -f "${DEPLOYMENT_FILE}.bak" 2>/dev/null || true
    echo "✅ 태그 변경됨 - Kubernetes가 자동으로 rolling update 수행"
  else
    echo "ℹ️  이미지 태그 변경 없음: $NEW_IMAGE"
  fi
fi

# k8s 배포
echo "📦 k8s 리소스 적용 중..."
$KUBECTL_CMD apply -f "$DEPLOY_PATH/"

# 배포 상태 확인
APP_NAME=$(grep -E "^  name:" "$DEPLOYMENT_FILE" | head -1 | awk '{print $2}')
if [ -n "$APP_NAME" ]; then
  # 이미지 태그가 latest인 경우에만 재시작 필요 (Kubernetes가 변경을 감지하지 못함)
  CURRENT_IMAGE=$(grep -E "^\s+image:" "$DEPLOYMENT_FILE" | head -1 | awk '{print $2}')
  if [[ "$CURRENT_IMAGE" == *":latest" ]]; then
    echo "⚠️  latest 태그 사용 중 - Pod 재시작 필요 (Kubernetes가 변경 감지 불가)"
    $KUBECTL_CMD rollout restart deployment/"$APP_NAME" -n baro-prod || true
  else
    echo "✅ 구체적인 태그 사용 - Kubernetes가 자동으로 rolling update 수행"
  fi
  
  echo "⏳ Pod가 Ready 상태가 될 때까지 대기 중..."
  $KUBECTL_CMD wait --for=condition=ready pod -l app="$APP_NAME" -n baro-prod --timeout=300s || {
    echo "⚠️  Pod가 Ready 상태가 되지 않았습니다. 로그를 확인하세요."
    $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME"
    exit 1
  }
  
  echo "✅ 배포 완료: $MODULE_NAME"
  $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME"
else
  echo "✅ 리소스 적용 완료: $MODULE_NAME"
fi

