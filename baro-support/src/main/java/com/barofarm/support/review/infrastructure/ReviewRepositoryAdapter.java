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

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(UUID uuid) {
        return reviewJpaRepository.findById(uuid);
    }

    @Override
    public void deleteById(UUID id) {
        reviewJpaRepository.deleteById(id);
    }

    @Override
    public Page<Review> findByProductIdAndStatusIn(UUID productId,
                                        Set<ReviewStatus> statuses,
                                        Pageable pageable) {
        return reviewJpaRepository.findByProductIdAndStatusIn(productId, statuses, pageable);
    }

    @Override
    public Page<Review> findByBuyerIdAndStatusIn(UUID buyerId,
                                      Set<ReviewStatus> statuses,
                                      Pageable pageable) {
        return reviewJpaRepository.findByBuyerIdAndStatusIn(buyerId, statuses, pageable);
    }

    @Override
    public boolean existsByOrderItemId(UUID orderItemId) {
        return reviewJpaRepository.existsByOrderItemId(orderItemId);
    }
}
