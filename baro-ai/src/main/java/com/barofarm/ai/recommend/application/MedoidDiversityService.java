package com.barofarm.ai.recommend.application;

import com.barofarm.ai.search.domain.ProductDocument;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 벡터 유사도 검색 결과에 메도이드 알고리즘을 적용하여
 * 다양성이 확보된 대표 상품들을 선택하는 서비스입니다.
 */
@Slf4j
@Service
public class MedoidDiversityService {

    /**
     * 후보 상품 리스트에서 메도이드 알고리즘을 사용해 다양한 대표 상품을 선택합니다.
     *
     * @param queryVector 쿼리 벡터 (최종 정렬에 사용, null 가능)
     * @param candidates  벡터를 포함한 후보 상품 리스트
     * @param topK        최종으로 선택할 상품 개수
     * @return 선택된 메도이드 상품 리스트
     */
    public List<ProductDocument> selectDiverseMedoids(
        float[] queryVector,
        List<ProductDocument> candidates,
        int topK
    ) {
        if (topK <= 0 || candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        // 벡터가 없는 상품은 제외
        List<ProductDocument> validCandidates = new ArrayList<>();
        for (ProductDocument candidate : candidates) {
            if (candidate != null && candidate.getVector() != null && candidate.getVector().length > 0) {
                validCandidates.add(candidate);
            }
        }

        if (validCandidates.isEmpty()) {
            log.debug("메도이드 선택을 위한 유효한 후보가 없습니다. candidates size={}", candidates.size());
            return List.of();
        }

        int k = Math.min(topK, validCandidates.size());

        // 후보 수가 topK 이하라면 ES가 이미 유사도 순으로 정렬했다고 보고 그대로 반환
        if (validCandidates.size() <= k) {
            return new ArrayList<>(validCandidates.subList(0, k));
        }

        // k-medoids 방식으로 후보들 사이의 거리를 기준으로 대표 상품 선택
        List<ProductDocument> medoids = new ArrayList<>();
        // 첫 번째 후보(가장 유사도 높은 상품)를 초기 메도이드로 사용
        medoids.add(validCandidates.get(0));

        // 각 후보가 현재까지 선택된 메도이드들과의 최소 거리를 저장
        double[] minDistances = new double[validCandidates.size()];
        for (int i = 0; i < minDistances.length; i++) {
            minDistances[i] = Double.POSITIVE_INFINITY;
        }

        while (medoids.size() < k) {
            int nextIndex = -1;
            double maxMinDistance = -1.0;

            for (int i = 0; i < validCandidates.size(); i++) {
                ProductDocument candidate = validCandidates.get(i);
                if (medoids.contains(candidate)) {
                    continue;
                }

                // 마지막으로 추가된 메도이드와의 거리만 계산하고, 기존 최소 거리와 비교/갱신
                ProductDocument lastMedoid = medoids.get(medoids.size() - 1);
                double distanceToLastMedoid = cosineDistance(candidate.getVector(), lastMedoid.getVector());
                if (distanceToLastMedoid < 0) {
                    continue;
                }

                double newMinDistance = Math.min(minDistances[i], distanceToLastMedoid);
                minDistances[i] = newMinDistance;

                if (newMinDistance > maxMinDistance) {
                    maxMinDistance = newMinDistance;
                    nextIndex = i;
                }
            }

            if (nextIndex < 0) {
                // 더 이상 유효한 후보가 없으면 종료
                break;
            }

            medoids.add(validCandidates.get(nextIndex));
        }

        // 최종적으로는 쿼리 벡터와의 유사도 기준으로 정렬 (있다면)
        if (queryVector != null) {
            medoids.sort((a, b) -> {
                double simA = cosineSimilarity(a.getVector(), queryVector);
                double simB = cosineSimilarity(b.getVector(), queryVector);
                return Double.compare(simB, simA); // 유사도 내림차순
            });
        }

        log.debug("메도이드 선택 완료: 후보 {}개 중 {}개 선택 (topK={})",
            validCandidates.size(), medoids.size(), topK);

        return medoids;
    }

    // 코사인 거리 = 1 - 코사인 유사도
    private double cosineDistance(float[] v1, float[] v2) {
        double sim = cosineSimilarity(v1, v2);
        if (sim < 0) {
            return -1.0;
        }
        return 1.0 - sim;
    }

    // 코사인 유사도 계산
    private double cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length == 0 || v2.length == 0 || v1.length != v2.length) {
            return -1.0;
        }

        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            double a = v1[i];
            double b = v2[i];
            dot += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return -1.0;
        }

        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
