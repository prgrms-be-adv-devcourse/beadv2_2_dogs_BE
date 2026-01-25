package com.barofarm.ai.review.application.bestreview;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.barofarm.ai.review.infrastructure.bestreview.ReviewProductIdCollector;
import com.barofarm.ai.review.infrastructure.bestreview.ReviewProductIdCollector.ProductIdPage;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BestReviewBatchJob {

    private static final int PAGE_SIZE = 1000;

    private final ReviewProductIdCollector productIdCollector;
    private final BestReviewListService listService;

    @Scheduled(cron = "${best-review.batch.cron:0 0 2 * * *}")
    public void run() {
        Map<String, FieldValue> afterKey = null;
        while (true) {
            ProductIdPage page;
            try {
                page = productIdCollector.fetchPage(PAGE_SIZE, afterKey);
            } catch (IOException e) {
                log.warn("Failed to fetch productIds.", e);
                return;
            }

            for (String productId : page.productIds()) {
                listService.refreshProduct(productId);
            }

            afterKey = page.afterKey();
            if (afterKey == null || afterKey.isEmpty()) {
                break;
            }
        }
    }
}
