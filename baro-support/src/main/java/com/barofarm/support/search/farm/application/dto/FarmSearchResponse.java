package com.barofarm.support.search.farm.application.dto;

import com.barofarm.support.search.farm.domain.FarmDocument;
import java.util.List;

// 검색 결과 총 건수와 문서 리스트를 단순 래핑
public record FarmSearchResponse(long total, List<FarmDocument> farms) {}
