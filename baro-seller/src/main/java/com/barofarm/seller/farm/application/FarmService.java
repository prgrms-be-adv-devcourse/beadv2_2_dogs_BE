package com.barofarm.seller.farm.application;

import com.barofarm.seller.farm.application.dto.request.FarmCreateCommand;
import com.barofarm.seller.farm.application.dto.request.FarmUpdateCommand;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.exception.FarmException;
import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.domain.FarmRepository;
import com.barofarm.seller.seller.exception.SellerException;
import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import static com.barofarm.seller.farm.exception.FarmErrorCode.FARM_NOT_FOUND;
import static com.barofarm.seller.seller.exception.SellerErrorCode.SELLER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmRepository farmRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public ResponseEntity<FarmCreateInfo> createFarm(UUID sellerId, FarmCreateCommand command) {

        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new SellerException(SELLER_NOT_FOUND));

        Farm farm = Farm.of(
            command.name(),
            command.description(),
            command.address(),
            command.phone(),
            seller
        );

        Farm saved = farmRepository.save(farm);

        return ResponseEntity.status(HttpStatus.CREATED).body(FarmCreateInfo.from(saved));
    }

    @Transactional
    public ResponseEntity<FarmUpdateInfo> updateFarm(UUID farmId, FarmUpdateCommand command) {

        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new FarmException(FARM_NOT_FOUND));

        farm.update(command.name(), command.description(), command.address(), command.phone());

        return ResponseEntity.status(HttpStatus.OK).body(FarmUpdateInfo.from(farm));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<FarmDetailInfo> findFarm(UUID farmId) {

        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new FarmException(FARM_NOT_FOUND));

        return ResponseEntity.status(HttpStatus.OK).body(FarmDetailInfo.from(farm));
    }

    @Transactional
    public ResponseEntity<Void> deleteFarm(UUID farmId) {
        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new FarmException(FARM_NOT_FOUND));

        farmRepository.deleteById(farmId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<FarmDetailInfo>> findFarmList(Pageable pageable) {
        Page<Farm> page = farmRepository.findAll(pageable);
        List<FarmDetailInfo> farms = page.stream()
            .map(FarmDetailInfo::from)
            .toList();

        return ResponseEntity.status(HttpStatus.OK).body(farms);
    }
}
