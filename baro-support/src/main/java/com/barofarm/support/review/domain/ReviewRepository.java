package com.barofarm.support.review.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(UUID uuid);

    void deleteById(UUID id);

    Page<Review> findByProductId(UUID productId, Pageable pageable);

    Page<Review> findByBuyerId(UUID buyerId, Pageable pageable);
}
