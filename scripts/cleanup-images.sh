#!/bin/bash

# ===================================
# Docker 이미지 정리 스크립트
# 오래된 이미지 자동 정리 (최근 N개 버전만 유지)
# Usage: bash cleanup-images.sh [KEEP_COUNT]
# Example: bash cleanup-images.sh 5  (최근 5개 버전만 유지)
# ===================================

set -e

# 색상
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ===================================
# 설정
# ===================================
KEEP_COUNT=${1:-5}  # 기본값: 최근 5개 유지
GITHUB_USERNAME="${GITHUB_USERNAME:-do-develop-space}"

log_info "🧹 Starting image cleanup..."
log_info "📦 Policy: Keep $KEEP_COUNT most recent versions PER MODULE"
log_info "🎯 Target: 8 modules × ${KEEP_COUNT} = $((8 * KEEP_COUNT)) images (max)"
echo ""

# ===================================
# 1. 미사용 이미지 정리
# ===================================
log_info "📦 Removing unused images..."
docker image prune -f

# ===================================
# 2. 모듈별 오래된 이미지 정리
# ===================================
MODULES=("baro-auth" "baro-buyer" "baro-seller" "baro-order" "baro-support" "eureka" "gateway" "config")

for MODULE in "${MODULES[@]}"; do
    log_info "Processing: $MODULE"
    
    # 해당 모듈의 모든 이미지 (태그별로)
    IMAGES=$(docker images "ghcr.io/${GITHUB_USERNAME}/${MODULE}" --format "{{.ID}} {{.CreatedAt}}" | sort -k2 -r)
    
    if [ -z "$IMAGES" ]; then
        log_warn "  No images found for $MODULE"
        continue
    fi
    
    # 이미지 개수 확인
    IMAGE_COUNT=$(echo "$IMAGES" | wc -l)
    log_info "  Found $IMAGE_COUNT versions"
    
    # 최근 N개만 유지
    if [ $IMAGE_COUNT -gt $KEEP_COUNT ]; then
        DELETE_COUNT=$((IMAGE_COUNT - KEEP_COUNT))
        log_warn "  Deleting $DELETE_COUNT old versions..."
        
        echo "$IMAGES" | tail -n $DELETE_COUNT | while read IMAGE_ID _; do
            # 실행 중인 컨테이너가 사용하는 이미지는 건너뛰기
            if docker ps -a --filter "ancestor=$IMAGE_ID" --format "{{.ID}}" | grep -q .; then
                log_warn "  Skipping $IMAGE_ID (in use)"
            else
                docker rmi "$IMAGE_ID" 2>/dev/null || log_warn "  Failed to remove $IMAGE_ID"
            fi
        done
        
        log_info "  ✅ Cleanup completed for $MODULE"
    else
        log_info "  ✅ No cleanup needed (${IMAGE_COUNT} <= ${KEEP_COUNT})"
    fi
done

# ===================================
# 3. Dangling 이미지 정리
# ===================================
log_info "🗑️  Removing dangling images..."
docker image prune -f

# ===================================
# 4. 볼륨 정리 (선택사항, 주의 필요)
# ===================================
# log_warn "⚠️  Cleaning unused volumes (commented out for safety)..."
# docker volume prune -f

# ===================================
# 5. 결과 요약
# ===================================
log_info "📊 Current disk usage:"
docker system df

log_info "🎉 Cleanup completed!"
log_info "Kept most recent $KEEP_COUNT versions for each module"

