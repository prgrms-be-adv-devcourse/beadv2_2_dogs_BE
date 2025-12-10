package com.barofarm.support.review.infrastructure;

import com.barofarm.support.review.domain.Review;
import com.barofarm.support.review.domain.ReviewRepository;
import com.barofarm.support.review.domain.ReviewStatus;
import java.util.Optional;
import java.util.Set;
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
    public Page<Review> findByProductIdAndStatusIn(UUID productId,
                                        Set<ReviewStatus> statuses,
                                        Pageable pageable) {
        return reviewRepository.findByProductIdAndStatusIn(productId, statuses, pageable);
    }

    @Override
    public Page<Review> findByBuyerIdAndStatusIn(UUID buyerId,
                                      Set<ReviewStatus> statuses,
                                      Pageable pageable) {
        return reviewRepository.findByBuyerIdAndStatusIn(buyerId, statuses, pageable);
    }

    @Override
    public boolean existsByOrderItemId(UUID orderItemId) {
        return reviewRepository.existsByOrderItemId(orderItemId);
    }
}
