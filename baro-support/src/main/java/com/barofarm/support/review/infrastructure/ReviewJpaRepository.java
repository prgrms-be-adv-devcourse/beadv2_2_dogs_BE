package com.barofarm.support.review.infrastructure;

import com.barofarm.support.review.domain.Review;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {
}
