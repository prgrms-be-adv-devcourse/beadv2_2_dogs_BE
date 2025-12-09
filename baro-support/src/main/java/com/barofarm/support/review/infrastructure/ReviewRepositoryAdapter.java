package com.barofarm.support.review.infrastructure;

import com.barofarm.support.review.domain.Review;
import com.barofarm.support.review.domain.ReviewRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewRepository reviewRepository;

    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public Optional<Review> findById(UUID uuid) {
        return reviewRepository.findById(uuid);
    }

    @Override
    public void deleteById(UUID id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public Page<Review> findByProductId(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable);
    }

    @Override
    public Page<Review> findByBuyerId(UUID buyerId, Pageable pageable) {
        return reviewRepository.findByBuyerId(buyerId, pageable);
    }
}
