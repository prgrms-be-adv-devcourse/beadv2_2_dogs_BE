#!/bin/bash

# ===================================
# 모듈별 배포 스크립트
# Usage: bash deploy-module.sh [MODULE_NAME]
# Example: bash deploy-module.sh auth
# ===================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# ===================================
# Docker Compose 명령어 감지
# ===================================
detect_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        echo "docker-compose"
    elif docker compose version &> /dev/null; then
        echo "docker compose"
    else
        log_error "Docker Compose not found. Please install Docker Compose."
        exit 1
    fi
}

DOCKER_COMPOSE=$(detect_docker_compose)
log_info "Using Docker Compose command: $DOCKER_COMPOSE"

# Docker Compose 명령어를 함수로 래핑하여 안전하게 사용
docker_compose_cmd() {
    if [[ "$DOCKER_COMPOSE" == "docker compose" ]]; then
        docker compose "$@"
    else
        docker-compose "$@"
    fi
}

# ===================================
# 파라미터 검증
# ===================================
MODULE_NAME=$1

if [ -z "$MODULE_NAME" ]; then
    log_error "모듈 이름을 지정해주세요."
    echo "Usage: bash deploy-module.sh [MODULE_NAME]"
    echo ""
    echo "Available modules:"
    echo "  - data    (데이터 인프라: Redis, MySQL, Kafka, Elasticsearch)"
    echo "  - cloud   (Spring Cloud: Eureka, Gateway, Config)"
    echo "  - infra   (data + cloud)"
    echo "  - auth    (인증 모듈)"
    echo "  - buyer   (구매자 모듈)"
    echo "  - seller  (판매자 모듈)"
    echo "  - order   (주문 모듈)"
    echo "  - support (지원 모듈)"
    echo "  - all     (전체 배포)"
    exit 1
fi

# ===================================
# 환경 변수
# ===================================
GITHUB_USERNAME="${GITHUB_USERNAME:-do-develop-space}"
PROJECT_DIR="${HOME}/apps/BE"

# Docker Compose 프로젝트 이름 설정
# Docker Compose는 디렉토리 이름을 기본 프로젝트 이름으로 사용하므로
# be_baro-network를 사용하도록 설정
export COMPOSE_PROJECT_NAME="be"

# 디렉토리 생성 (없으면)
mkdir -p ${PROJECT_DIR}

cd ${PROJECT_DIR}

log_info "🚀 Deploying module: ${MODULE_NAME}"

# ===================================
# 1. GitHub Container Registry 로그인
# ===================================
log_step "📦 Logging in to GitHub Container Registry..."
if [ -n "${GITHUB_TOKEN}" ]; then
    echo "${GITHUB_TOKEN}" | docker login ghcr.io -u "${GITHUB_USERNAME}" --password-stdin
    if [ $? -eq 0 ]; then
        log_info "✅ Successfully logged in to GHCR"
    else
        log_error "❌ Failed to login to GHCR. Please check your token permissions."
        log_warn "💡 Tip: Use GHCR_PAT (Personal Access Token) with 'read:packages' permission"
        exit 1
    fi
else
    log_warn "GITHUB_TOKEN not set, skipping registry login"
    log_warn "⚠️  Private images may fail to pull without authentication"
fi

# ===================================
# 1.5. Docker 네트워크 확인 및 생성
# ===================================
log_step "🌐 Checking Docker network..."
# Docker Compose 프로젝트 이름 설정 (이미 위에서 설정했지만 명시적으로 다시 설정)
export COMPOSE_PROJECT_NAME="be"

# be_baro-network 확인 및 생성 (Docker Compose가 프로젝트 이름을 접두사로 붙임)
if docker network ls --format '{{.Name}}' | grep -q "^be_baro-network$"; then
    log_info "✅ Found be_baro-network"
else
    log_info "Creating be_baro-network..."
    # 네트워크 생성 시도 (이미 존재하면 에러 무시)
    CREATE_OUTPUT=$(docker network create be_baro-network 2>&1)
    CREATE_EXIT_CODE=$?
    
    if [ $CREATE_EXIT_CODE -eq 0 ]; then
        log_info "✅ Created be_baro-network"
    elif echo "$CREATE_OUTPUT" | grep -q "already exists"; then
        log_info "✅ be_baro-network already exists"
    else
        # 실제로 생성 실패한 경우에만 에러
        log_error "❌ Failed to create be_baro-network: $CREATE_OUTPUT"
        exit 1
    fi
fi

# ===================================
# 2. 인프라 서비스 확인
# ===================================
check_data_infra() {
    log_step "🔍 Checking data infrastructure..."
    # MySQL, Kafka가 모두 실행 중인지 확인
    MYSQL_RUNNING=$(docker ps --format '{{.Names}}' | grep -q "^baro-mysql$" && echo "yes" || echo "no")
    KAFKA_RUNNING=$(docker ps --format '{{.Names}}' | grep -q "^baro-kafka$" && echo "yes" || echo "no")
    
    if [ "$MYSQL_RUNNING" = "no" ] || [ "$KAFKA_RUNNING" = "no" ]; then
        log_warn "Data infrastructure not running. Starting data infrastructure first..."
        # Elasticsearch는 build가 필요하므로 build 먼저 시도
        if [ -f "docker/baro-es/Dockerfile" ]; then
            log_info "Building Elasticsearch image..."
            if docker_compose_cmd -f docker-compose.data.yml build elasticsearch 2>&1; then
                log_info "✅ Elasticsearch image built successfully"
            else
                log_warn "⚠️ Elasticsearch build failed, will try to use existing image or skip"
            fi
        fi
        # pull은 build가 필요한 이미지는 제외하고 실행
        docker_compose_cmd -f docker-compose.data.yml pull mysql kafka 2>/dev/null || true
        # Elasticsearch가 없어도 MySQL, Kafka는 시작
        docker_compose_cmd -f docker-compose.data.yml up -d mysql kafka
        # Elasticsearch는 별도로 시도 (실패해도 계속 진행)
        docker_compose_cmd -f docker-compose.data.yml up -d elasticsearch 2>/dev/null || log_warn "⚠️ Elasticsearch start failed, continuing without it"
        log_info "Waiting for data infrastructure to be ready (20 seconds)..."
        sleep 20
    else
        log_info "✅ Data infrastructure is already running (MySQL: $MYSQL_RUNNING, Kafka: $KAFKA_RUNNING)."
    fi
    
    # MySQL이 실행 중이면 데이터베이스 초기화 확인 및 실행
    if [ "$MYSQL_RUNNING" = "yes" ]; then
        log_step "🔍 Checking MySQL databases..."
        # MySQL이 준비될 때까지 대기
        if docker exec baro-mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
            # 필수 데이터베이스가 있는지 확인
            REQUIRED_DBS=("baroauth" "baroseller" "barobuyer" "baroorder" "barosupport")
            MISSING_DBS=()
            
            for db in "${REQUIRED_DBS[@]}"; do
                if ! docker exec baro-mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD:-rootpassword}" -e "USE \`$db\`;" 2>/dev/null; then
                    MISSING_DBS+=("$db")
                fi
            done
            
            if [ ${#MISSING_DBS[@]} -gt 0 ]; then
                log_warn "⚠️ Missing databases detected: ${MISSING_DBS[*]}"
                log_info "Creating missing databases..."
                
                # SQL 스크립트 실행
                if [ -f "scripts/init-db/01-create-databases.sql" ]; then
                    docker exec -i baro-mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD:-rootpassword}" < scripts/init-db/01-create-databases.sql 2>/dev/null && \
                        log_info "✅ Databases created successfully" || \
                        log_warn "⚠️ Failed to create databases, but continuing..."
                else
                    log_warn "⚠️ Database initialization script not found: scripts/init-db/01-create-databases.sql"
                fi
            else
                log_info "✅ All required databases exist."
            fi
        else
            log_warn "⚠️ MySQL is not ready yet, skipping database check."
        fi
    fi
}

check_cloud_infra() {
    log_step "🔍 Checking Spring Cloud infrastructure..."
    if ! docker ps | grep -q baro-eureka; then
        log_warn "Spring Cloud infrastructure not running. Starting cloud infrastructure first..."
        # IMAGE_TAG 환경 변수가 설정되어 있으면 사용, 없으면 latest
        # 브랜치별 배포 시 동일한 태그 사용 (dev-support -> dev-support, main-support -> latest)
        local cloud_image_tag="${IMAGE_TAG:-latest}"
        log_info "Using image tag for cloud infrastructure: ${cloud_image_tag}"
        export IMAGE_TAG="${cloud_image_tag}"
        docker_compose_cmd -f docker-compose.cloud.yml pull
        docker_compose_cmd -f docker-compose.cloud.yml up -d
        log_info "Waiting for Spring Cloud to be ready (30 seconds)..."
        sleep 30
    else
        log_info "Spring Cloud infrastructure is already running."
    fi
}

# ===================================
# 3. 모듈별 배포
# ===================================
deploy_module() {
    local module=$1
    local compose_file="docker-compose.${module}.yml"
    
    if [ ! -f "$compose_file" ]; then
        log_error "Compose file not found: $compose_file"
        exit 1
    fi
    
    # 네트워크 확인
    if ! docker network ls --format '{{.Name}}' | grep -q "^be_baro-network$"; then
        log_error "❌ be_baro-network not found!"
        log_error "Please create the network first: docker network create be_baro-network"
        exit 1
    fi
    
    # IMAGE_TAG 환경 변수가 설정되어 있으면 사용, 없으면 latest
    export IMAGE_TAG="${IMAGE_TAG:-latest}"
    log_info "Using image tag: ${IMAGE_TAG}"
    
    # 현재 버전 기록 (롤백용)
    CURRENT_IMAGE=$(docker inspect "baro-${module}" --format='{{.Config.Image}}' 2>/dev/null || echo "none")
    
    log_step "📥 Pulling image for $module (tag: ${IMAGE_TAG})..."
    if ! docker_compose_cmd -f "$compose_file" pull; then
        log_error "❌ Failed to pull image for $module"
        exit 1
    fi
    
    # Pull한 이미지 정보
    NEW_IMAGE=$(docker_compose_cmd -f "$compose_file" config | grep "image:" | head -1 | awk '{print $2}')
    log_info "Image to deploy: $NEW_IMAGE"
    
    log_step "🛑 Stopping existing container for $module..."
    docker_compose_cmd -f "$compose_file" down || true
    
    log_step "🏃 Starting $module..."
    if ! docker_compose_cmd -f "$compose_file" up -d; then
        log_error "❌ Failed to start container for $module"
        log_error "Checking container logs..."
        docker logs baro-${module} --tail 50 2>&1 || echo "Container logs not available"
        exit 1
    fi
    
    # 컨테이너가 정상적으로 시작되었는지 확인
    sleep 3
    if ! docker ps | grep -q "baro-${module}"; then
        log_error "❌ Container baro-${module} is not running after start"
        log_error "Checking container status..."
        docker ps -a | grep "baro-${module}" || echo "Container not found"
        log_error "Checking container logs..."
        docker logs baro-${module} --tail 50 2>&1 || echo "Container logs not available"
        exit 1
    fi
    
    log_info "✅ Container baro-${module} is running"
    
    # 배포 이력 저장
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Deploy: $module | Previous: $CURRENT_IMAGE | New: $NEW_IMAGE" >> ${PROJECT_DIR}/deployment-history.log
    
    log_info "✅ Module $module deployed successfully!"
    log_info "📝 Deployment recorded in ${PROJECT_DIR}/deployment-history.log"
}

# ===================================
# 4. 전체 배포
# ===================================
deploy_all() {
    log_step "Deploying all modules..."
    
    # 1. 데이터 인프라
    log_info "Step 1/4: Deploying data infrastructure..."
        docker_compose_cmd -f docker-compose.data.yml pull
        docker_compose_cmd -f docker-compose.data.yml up -d
    sleep 20
    
    # 2. Spring Cloud 인프라
    log_info "Step 2/4: Deploying Spring Cloud infrastructure..."
        docker_compose_cmd -f docker-compose.cloud.yml pull
        docker_compose_cmd -f docker-compose.cloud.yml up -d
    sleep 30
    
    # 3. 비즈니스 모듈들
    log_info "Step 3/4: Deploying business modules..."
    for module in auth buyer seller order support; do
        deploy_module "$module"
        sleep 5
    done
    
    log_info "✅ All modules deployed successfully!"
}

# ===================================
# 메인 로직
# ===================================
case $MODULE_NAME in
    data)
        log_step "Deploying data infrastructure..."
        # 네트워크가 없으면 생성 (data 인프라가 네트워크를 생성함)
        if ! docker network ls --format '{{.Name}}' | grep -q "^be_baro-network$"; then
            log_info "Creating be_baro-network..."
            CREATE_OUTPUT=$(docker network create be_baro-network 2>&1)
            CREATE_EXIT_CODE=$?
            if [ $CREATE_EXIT_CODE -eq 0 ]; then
                log_info "✅ Created be_baro-network"
            elif echo "$CREATE_OUTPUT" | grep -q "already exists"; then
                log_info "✅ be_baro-network already exists"
            else
                log_error "❌ Failed to create be_baro-network: $CREATE_OUTPUT"
                exit 1
            fi
        fi
        # Elasticsearch 커스텀 이미지 빌드가 필요하므로 pull 대신 build
        log_step "🔨 Building Elasticsearch image (if needed)..."
        docker_compose_cmd -f docker-compose.data.yml build elasticsearch || log_warn "Build failed, trying pull..."
        docker_compose_cmd -f docker-compose.data.yml pull || true
        docker_compose_cmd -f docker-compose.data.yml down || true
        docker_compose_cmd -f docker-compose.data.yml up -d
        log_info "✅ Data infrastructure deployed successfully!"
        ;;
    
    cloud)
        log_step "Deploying Spring Cloud infrastructure..."
        # 네트워크 확인
        if ! docker network ls --format '{{.Name}}' | grep -q "^be_baro-network$"; then
            log_error "❌ be_baro-network not found!"
            log_error "Please create the network first: docker network create be_baro-network"
            exit 1
        fi
        check_data_infra
        # IMAGE_TAG 환경 변수가 설정되어 있으면 사용, 없으면 latest
        export IMAGE_TAG="${IMAGE_TAG:-latest}"
        log_info "Using image tag for cloud infrastructure: ${IMAGE_TAG}"
        docker_compose_cmd -f docker-compose.cloud.yml pull
        docker_compose_cmd -f docker-compose.cloud.yml down || true
        docker_compose_cmd -f docker-compose.cloud.yml up -d
        log_info "✅ Spring Cloud infrastructure deployed successfully!"
        ;;
    
    infra)
        log_step "Deploying all infrastructure (data + cloud)..."
        # 네트워크 확인
        if ! docker network ls --format '{{.Name}}' | grep -q "^be_baro-network$"; then
            log_error "❌ be_baro-network not found!"
            log_error "Please create the network first: docker network create be_baro-network"
            exit 1
        fi
        # IMAGE_TAG 환경 변수가 설정되어 있으면 사용, 없으면 latest
        export IMAGE_TAG="${IMAGE_TAG:-latest}"
        log_info "Using image tag for infrastructure: ${IMAGE_TAG}"
        
        # 1. 데이터 인프라 배포 (이미 실행 중이면 건너뛰기)
        if docker ps | grep -q baro-mysql; then
            log_info "✅ Data infrastructure is already running. Skipping data deployment."
        else
            log_info "Step 1/2: Deploying data infrastructure..."
            docker_compose_cmd -f docker-compose.data.yml pull
            docker_compose_cmd -f docker-compose.data.yml down || true
            docker_compose_cmd -f docker-compose.data.yml up -d
            sleep 20
        fi
        
        # 2. Cloud 인프라 배포 (이미 실행 중이면 건너뛰기)
        # if docker ps | grep -q baro-eureka; then
        #     log_info "✅ Cloud infrastructure is already running. Skipping cloud deployment."
        # else
        #     log_info "Step 2/2: Deploying Spring Cloud infrastructure..."
        #     $DOCKER_COMPOSE -f docker-compose.cloud.yml pull
        #     $DOCKER_COMPOSE -f docker-compose.cloud.yml down || true
        #     $DOCKER_COMPOSE -f docker-compose.cloud.yml up -d
        #     sleep 30
        # fi
        
        log_info "✅ All infrastructure deployed successfully!"
        ;;
    
    auth|buyer|seller|order|support)
        # 데이터 인프라는 필수 (Redis, MySQL, Kafka)
        check_data_infra
        # Cloud 인프라는 선택적 (이미 실행 중이면 체크만, 없으면 경고만)
        if ! docker ps | grep -q baro-eureka; then
            log_warn "⚠️  Cloud infrastructure (Eureka, Gateway, Config) is not running."
            log_warn "⚠️  The module may not work properly without cloud infrastructure."
            log_warn "⚠️  If needed, deploy cloud infrastructure separately: bash deploy-module.sh cloud"
        else
            log_info "✅ Cloud infrastructure is already running."
        fi
        # 모듈만 단독 배포
        deploy_module "$MODULE_NAME"
        ;;
    
    # all)
    #     deploy_all
    #     ;;
    
    *)
        log_error "Unknown module: $MODULE_NAME"
        log_info "Available modules: data, cloud, infra, auth, buyer, seller, order, support"
        log_info "Unavailable modules: infra, all"
        exit 1
        ;;
esac

# ===================================
# 5. 상태 확인
# ===================================
log_step "🔍 Checking container status..."
docker ps --filter "name=baro-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# ===================================
# 6. 정리
# ===================================
log_step "🧹 Cleaning up unused Docker resources..."
docker system prune -f --volumes

log_info "🎉 Deployment completed!"

