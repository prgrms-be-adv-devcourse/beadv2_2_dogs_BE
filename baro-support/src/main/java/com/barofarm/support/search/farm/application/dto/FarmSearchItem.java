package com.barofarm.support.search.farm.application.dto;

import java.util.UUID;

// 프론트에 보여줄 농장 List Item
public record FarmSearchItem(
    UUID farmId,
    String farmName, // 농장명
    String farmAddress // 농장 주소
) {}
