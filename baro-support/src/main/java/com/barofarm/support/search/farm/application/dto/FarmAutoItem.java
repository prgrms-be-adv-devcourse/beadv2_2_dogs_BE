package com.barofarm.support.search.farm.application.dto;

import java.util.UUID;

public record FarmAutoItem(
    UUID farmId, // 프론트에서 클릭 시 농장으로 바로 이동
    String farmName, // 농장명
    String type // 타입
) {
    public FarmAutoItem(UUID farmId, String farmName) {
        this(farmId, farmName, "farm");
    }
}
