package com.barofarm.seller.farm.infrastructure;

import com.barofarm.seller.farm.domain.FarmImage;
import com.barofarm.seller.farm.domain.FarmImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FarmImageRepositoryAdapter implements FarmImageRepository {

    private final FarmImageJpaRepository farmImageJpaRepository;

    @Override
    public FarmImage save(FarmImage farmImage) {
        return farmImageJpaRepository.save(farmImage);
    }
}
