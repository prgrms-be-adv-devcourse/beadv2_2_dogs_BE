package com.barofarm.ai.review.application.bestreview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class BestReviewReplacementPolicy {

    public boolean shouldReplace(List<String> existingIds, List<String> newIds, double threshold) {
        if (existingIds == null || existingIds.isEmpty()) {
            return true;
        }
        int existingSize = existingIds.size();
        int replaced = countReplaced(existingIds, newIds);
        double rate = (double) replaced / existingSize;
        return rate >= threshold;
    }

    private int countReplaced(List<String> existingIds, List<String> newIds) {
        Set<String> existingSet = new HashSet<>(existingIds);
        Set<String> newSet = (newIds == null) ? Set.of() : new HashSet<>(newIds);

        int intersection = 0;
        for (String id : existingSet) {
            if (newSet.contains(id)) {
                intersection++;
            }
        }
        return existingSet.size() - intersection;
    }
}
