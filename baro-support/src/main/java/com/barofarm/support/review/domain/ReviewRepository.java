package com.barofarm.support.review.domain;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(UUID uuid);

    void deleteById(UUID id);

    Page<Review> findByProductIdAndStatusIn(UUID productId,
                                            Set<ReviewStatus> statuses,
                                            Pageable pageable);

    Page<Review> findByBuyerIdAndStatusIn(UUID buyerId,
                                          Set<ReviewStatus> statuses,
                                          Pageable pageable);

    boolean existsByOrderItemId(UUID orderItemId);
}
