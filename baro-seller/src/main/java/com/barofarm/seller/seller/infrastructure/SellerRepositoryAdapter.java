package com.barofarm.seller.seller.infrastructure;

import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.SellerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SellerRepositoryAdapter implements SellerRepository {

    private final SellerJpaRepository sellerJpaRepository;

    @Override
    public Seller save(Seller seller) {
        return sellerJpaRepository.save(seller);
    }

    @Override
    public Optional<Seller> findById(UUID id) {
        return sellerJpaRepository.findById(id);
    }

    @Override
    public List<Seller> findByIdIn(List<UUID> userIds) {
        return sellerJpaRepository.findByIdIn(userIds);
    }

}
