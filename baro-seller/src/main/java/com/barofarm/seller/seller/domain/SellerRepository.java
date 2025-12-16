package com.barofarm.seller.seller.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// port.out성격
public interface SellerRepository {

    Seller save(Seller seller);
    Optional<Seller> findById(UUID id);

    List<Seller> findByIdIn(List<UUID> userIds);
}
