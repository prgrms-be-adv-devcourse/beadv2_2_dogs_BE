package com.barofarm.support.search.experience.application.dto;

import com.barofarm.support.search.experience.domain.ExperienceDocument;
import java.util.List;

// 검색 결과 총 건수와 문서 리스트를 단순 래핑
public record ExperienceSearchResponse(long total, List<ExperienceDocument> experiences) {}
