package com.barofarm.support.review.infrastructure;

import com.barofarm.support.review.domain.Review;
import com.barofarm.support.review.domain.ReviewStatus;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdAndStatusIn(
        UUID productId,
        Set<ReviewStatus> statuses,
        Pageable pageable
    );

    Page<Review> findByBuyerIdAndStatusIn(
        UUID buyerId,
        Set<ReviewStatus> statuses,
        Pageable pageable
    );

    boolean existsByOrderItemId(UUID orderItemId);
}
