package com.barofarm.seller.farm.application;

import static com.barofarm.seller.farm.exception.FarmErrorCode.FARM_FORBIDDEN;
import static com.barofarm.seller.farm.exception.FarmErrorCode.FARM_NOT_FOUND;
import static com.barofarm.seller.seller.exception.SellerErrorCode.SELLER_NOT_FOUND;

import com.barofarm.dto.CustomPage;
import com.barofarm.dto.ResponseDto;
import com.barofarm.exception.CustomException;
import com.barofarm.seller.farm.application.dto.request.FarmCreateCommand;
import com.barofarm.seller.farm.application.dto.request.FarmUpdateCommand;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmListInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.application.event.FarmEventPublisher;
import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.domain.FarmImageRepository;
import com.barofarm.seller.farm.domain.FarmRepository;
import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.SellerRepository;
import com.barofarm.storage.s3.S3ImageUploader;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmRepository farmRepository;
    private final SellerRepository sellerRepository;
    private final S3ImageUploader s3ImageUploader;
    private final FarmImageRepository farmImageRepository;
    private final FarmEventPublisher farmEventPublisher;

    @Transactional
    public ResponseDto<FarmCreateInfo> createFarm(UUID sellerId, FarmCreateCommand command, MultipartFile image) {

        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new CustomException(SELLER_NOT_FOUND));

        Farm.Details details = new Farm.Details(
            command.name(),
            command.description(),
            command.address(),
            command.phone(),
            command.email(),
            command.establishedYear(),
            command.farmSize(),
            command.cultivationMethod()
        );

        Farm farm = Farm.of(details, seller);

        if (image != null && !image.isEmpty()) {
            S3ImageUploader.UploadedImage uploaded = null;
            try {
                String category = "farms/" + farm.getId();
                uploaded = s3ImageUploader.uploadWebpImage(category, image);
                farm.setImage(uploaded.url(), uploaded.key());
            } catch (Exception e) {
                if (uploaded != null) {
                    s3ImageUploader.deleteObject(uploaded.key());
                }
                throw e;
            }
        }

        Farm saved = farmRepository.save(farm);

        // Farm 생성 이벤트 발행 (비동기)
        try {
            farmEventPublisher.publishFarmCreated(saved);
        } catch (Exception e) {
            // 이벤트 발행 실패는 Farm 생성 실패로 이어지지 않음
            // 로깅만 수행하고 계속 진행
            log.error("Farm 생성 이벤트 발행 실패: farmId={}", saved.getId(), e);
        }

        return ResponseDto.ok(FarmCreateInfo.from(saved));
    }

    @Transactional
    public ResponseDto<FarmUpdateInfo> updateFarm(UUID sellerId, UUID farmId,
                                                  FarmUpdateCommand command, MultipartFile image) {

        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new CustomException(FARM_NOT_FOUND));

        if (!farm.getSeller().getId().equals(sellerId)) {
            throw new CustomException(FARM_FORBIDDEN);
        }

        Farm.Details details = new Farm.Details(
            command.name(),
            command.description(),
            command.address(),
            command.phone(),
            command.email(),
            command.establishedYear(),
            command.farmSize(),
            command.cultivationMethod()
        );

        farm.update(details);

        // Farm 수정 이벤트 발행 (비동기)
        try {
            farmEventPublisher.publishFarmUpdated(farm);
        } catch (Exception e) {
            log.error("Farm 수정 이벤트 발행 실패: farmId={}", farm.getId(), e);
        }

        String oldKey = (farm.getImage() != null) ? farm.getImage().getS3Key() : null;

        if (image != null && !image.isEmpty()) {
            S3ImageUploader.UploadedImage uploaded = null;
            try {
                String category = "farms/" + farm.getId();
                uploaded = s3ImageUploader.uploadWebpImage(category, image);
                farm.setImage(uploaded.url(), uploaded.key());

                if (oldKey != null) {
                    s3ImageUploader.deleteObject(oldKey);
                }
            } catch (Exception e) {
                if (uploaded != null) {
                    s3ImageUploader.deleteObject(uploaded.key());
                }
                throw e;
            }
        } else {
            if (oldKey != null) {
                farm.removeImage();

                s3ImageUploader.deleteObject(oldKey);
            }
        }

        return ResponseDto.ok(FarmUpdateInfo.from(farm));
    }

    @Transactional(readOnly = true)
    public ResponseDto<FarmDetailInfo> findFarm(UUID farmId) {

        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new CustomException(FARM_NOT_FOUND));

        return ResponseDto.ok(FarmDetailInfo.from(farm));
    }


    @Transactional
    public ResponseDto<Void> deleteFarm(UUID sellerId, UUID farmId) {
        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new CustomException(FARM_NOT_FOUND));

        if (!farm.getSeller().getId().equals(sellerId)) {
            throw new CustomException(FARM_FORBIDDEN);
        }

        // Farm 삭제 이벤트 발행 (비동기, 삭제 전에 발행)
        try {
            farmEventPublisher.publishFarmDeleted(farm);
        } catch (Exception e) {
            log.error("Farm 삭제 이벤트 발행 실패: farmId={}", farm.getId(), e);
        }

        farmRepository.delete(farm);
        return ResponseDto.ok(null);
    }

    @Transactional(readOnly = true)
    public ResponseDto<CustomPage<FarmListInfo>> findFarmList(Pageable pageable) {
        Page<Farm> page = farmRepository.findAll(pageable);
        Page<FarmListInfo> dtoPage = page.map(FarmListInfo::from);
        return ResponseDto.ok(CustomPage.from(dtoPage));
    }

    @Transactional(readOnly = true)
    public ResponseDto<CustomPage<FarmListInfo>> findMyFarmList(UUID sellerId, Pageable pageable) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new CustomException(SELLER_NOT_FOUND));

        Page<Farm> page = farmRepository.findBySeller(seller, pageable);
        Page<FarmListInfo> dtoPage = page.map(FarmListInfo::from);

        return ResponseDto.ok(CustomPage.from(dtoPage));
    }
}
