package com.barofarm.support.search.farm.application.dto;

import java.util.UUID;

// 농장 색인 요청 DTO (updatedAt은 서버에서 자동 생성)
public record FarmIndexRequest(
    UUID farmId, String farmName, String farmAddress, String status) {}
