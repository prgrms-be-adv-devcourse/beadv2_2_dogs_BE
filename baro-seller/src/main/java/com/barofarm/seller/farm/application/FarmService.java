package com.barofarm.seller.farm.application;

import static com.barofarm.seller.farm.exception.FarmErrorCode.FARM_FORBIDDEN;
import static com.barofarm.seller.farm.exception.FarmErrorCode.FARM_NOT_FOUND;
import static com.barofarm.seller.seller.exception.SellerErrorCode.SELLER_NOT_FOUND;

import com.barofarm.seller.common.exception.CustomException;
import com.barofarm.seller.common.response.CustomPage;
import com.barofarm.seller.common.response.ResponseDto;
import com.barofarm.seller.farm.application.dto.request.FarmCreateCommand;
import com.barofarm.seller.farm.application.dto.request.FarmUpdateCommand;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.domain.FarmRepository;
import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.SellerRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmRepository farmRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public ResponseDto<FarmCreateInfo> createFarm(UUID sellerId, FarmCreateCommand command) {

        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new CustomException(SELLER_NOT_FOUND));

        Farm farm = Farm.of(
            command.name(),
            command.description(),
            command.address(),
            command.phone(),
            seller
        );

        Farm saved = farmRepository.save(farm);
        return ResponseDto.ok(FarmCreateInfo.from(saved));
    }

    @Transactional
    public ResponseDto<FarmUpdateInfo> updateFarm(UUID mockSellerId, UUID farmId, FarmUpdateCommand command) {

        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new CustomException(FARM_NOT_FOUND));

        if (!farm.getSeller().getId().equals(mockSellerId)) {
            throw new CustomException(FARM_FORBIDDEN);
        }

        farm.update(command.name(), command.description(), command.address(), command.phone());

        return ResponseDto.ok(FarmUpdateInfo.from(farm));
    }

    @Transactional(readOnly = true)
    public ResponseDto<FarmDetailInfo> findFarm(UUID farmId) {

        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new CustomException(FARM_NOT_FOUND));

        return ResponseDto.ok(FarmDetailInfo.from(farm));
    }

    @Transactional
    public ResponseDto<Void> deleteFarm(UUID mockSellerId, UUID farmId) {
        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new CustomException(FARM_NOT_FOUND));

        if (!farm.getSeller().getId().equals(mockSellerId)) {
            throw new CustomException(FARM_FORBIDDEN);
        }

        farmRepository.deleteById(farmId);

        return ResponseDto.ok(null);
    }

    @Transactional(readOnly = true)
    public ResponseDto<CustomPage<FarmDetailInfo>> findFarmList(Pageable pageable) {
        Page<FarmDetailInfo> page = farmRepository.findAll(pageable)
            .map(FarmDetailInfo::from);

        return ResponseDto.ok(CustomPage.from(page));
    }
}
