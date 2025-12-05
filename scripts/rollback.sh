#!/bin/bash

# ===================================
# 롤백 스크립트
# 이전 버전으로 안전하게 롤백
# Usage: bash rollback.sh [MODULE] [TAG]
# Example: bash rollback.sh auth main-auth-abc123
# ===================================

set -e

# 색상
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

# ===================================
# 파라미터 검증
# ===================================
MODULE_NAME=$1
TARGET_TAG=$2

if [ -z "$MODULE_NAME" ]; then
    log_error "모듈 이름을 지정해주세요."
    echo "Usage: bash rollback.sh [MODULE] [TAG]"
    echo ""
    echo "Available modules: auth, buyer, seller, order, support, cloud"
    echo ""
    echo "Examples:"
    echo "  bash rollback.sh auth main-auth-abc123"
    echo "  bash rollback.sh buyer main-buyer-20241205-143022"
    echo "  bash rollback.sh cloud latest"
    exit 1
fi

if [ -z "$TARGET_TAG" ]; then
    log_warn "태그를 지정하지 않았습니다. 사용 가능한 태그 목록을 확인하세요."
    echo ""
    echo "사용법:"
    echo "1. GitHub Packages에서 사용 가능한 태그 확인"
    echo "   https://github.com/dev2_team02_dogs_danny?tab=packages"
    echo ""
    echo "2. 또는 최근 배포 이력 확인"
    echo "   cat ~/deployment-history.log"
    echo ""
    echo "3. 태그를 지정하여 다시 실행"
    echo "   bash rollback.sh $MODULE_NAME [TAG]"
    exit 1
fi

# ===================================
# 환경 변수
# ===================================
GITHUB_USERNAME="${GITHUB_USERNAME:-do-develop-space}"
DOCKER_REGISTRY="ghcr.io/${GITHUB_USERNAME}"
PROJECT_DIR="${HOME}"

cd ${PROJECT_DIR}

log_info "🔄 Starting rollback for module: ${MODULE_NAME} to tag: ${TARGET_TAG}"

# ===================================
# 1. 현재 버전 백업
# ===================================
log_step "📸 Backing up current version..."
COMPOSE_FILE="docker-compose.${MODULE_NAME}.yml"

if [ ! -f "$COMPOSE_FILE" ]; then
    log_error "Compose file not found: $COMPOSE_FILE"
    exit 1
fi

# 현재 실행 중인 이미지 정보 저장
CURRENT_IMAGE=$(docker inspect "baro-${MODULE_NAME}" --format='{{.Config.Image}}' 2>/dev/null || echo "none")
log_info "Current image: $CURRENT_IMAGE"

# 배포 이력 저장
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Rollback: $MODULE_NAME from $CURRENT_IMAGE to $DOCKER_REGISTRY/baro-${MODULE_NAME}:$TARGET_TAG" >> ~/deployment-history.log

# ===================================
# 2. 타겟 이미지 Pull
# ===================================
log_step "📥 Pulling target image..."
TARGET_IMAGE="${DOCKER_REGISTRY}/baro-${MODULE_NAME}:${TARGET_TAG}"

if ! docker pull "$TARGET_IMAGE"; then
    log_error "Failed to pull image: $TARGET_IMAGE"
    log_info "사용 가능한 태그를 확인하세요: https://github.com/dev2_team02_dogs_danny?tab=packages"
    exit 1
fi

log_info "✅ Successfully pulled: $TARGET_IMAGE"

# ===================================
# 3. 기존 컨테이너 중지
# ===================================
log_step "🛑 Stopping current container..."
docker stop "baro-${MODULE_NAME}" 2>/dev/null || true

# 컨테이너 백업 (이름 변경)
BACKUP_NAME="baro-${MODULE_NAME}-backup-$(date '+%Y%m%d-%H%M%S')"
docker rename "baro-${MODULE_NAME}" "$BACKUP_NAME" 2>/dev/null || true
log_info "Backup container: $BACKUP_NAME"

# ===================================
# 4. 새 버전으로 시작
# ===================================
log_step "🏃 Starting with target version..."
export IMAGE_TAG=$TARGET_TAG
docker-compose -f "$COMPOSE_FILE" up -d

# ===================================
# 5. Health Check
# ===================================
log_step "🏥 Waiting for health check..."
sleep 10

# 포트 매핑 확인
case $MODULE_NAME in
    auth) PORT=8081 ;;
    buyer) PORT=8082 ;;
    seller) PORT=8085 ;;
    order) PORT=8087 ;;
    support) PORT=8089 ;;
    cloud) PORT=8080 ;;  # Gateway
    *) PORT=8080 ;;
esac

# Health check (최대 30초 대기)
for i in {1..10}; do
    if wget --spider -q "http://localhost:${PORT}/actuator/health" 2>/dev/null; then
        log_info "✅ Health check passed!"
        break
    fi
    if [ $i -eq 10 ]; then
        log_error "❌ Health check failed!"
        log_warn "Rolling back to previous version..."
        
        # 롤백 실패 시 이전 버전 복원
        docker stop "baro-${MODULE_NAME}" 2>/dev/null || true
        docker rm "baro-${MODULE_NAME}" 2>/dev/null || true
        docker rename "$BACKUP_NAME" "baro-${MODULE_NAME}" 2>/dev/null || true
        docker start "baro-${MODULE_NAME}" 2>/dev/null || true
        
        log_error "Rollback failed. Previous version restored."
        exit 1
    fi
    log_info "Waiting for service to be ready... ($i/10)"
    sleep 3
done

# ===================================
# 6. 백업 컨테이너 정리
# ===================================
log_step "🧹 Cleaning up backup container..."
docker rm "$BACKUP_NAME" 2>/dev/null || true

# ===================================
# 7. 완료
# ===================================
log_info "🎉 Rollback completed successfully!"
log_info "Module: $MODULE_NAME"
log_info "Version: $TARGET_TAG"
log_info "Container: baro-${MODULE_NAME}"

# 상태 표시
docker ps --filter "name=baro-${MODULE_NAME}" --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"

log_info "📝 Deployment history: ~/deployment-history.log"

