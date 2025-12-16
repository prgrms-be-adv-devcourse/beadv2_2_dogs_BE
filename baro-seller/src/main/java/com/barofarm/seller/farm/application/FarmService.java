package com.barofarm.seller.farm.application;

import static com.barofarm.seller.farm.exception.FarmErrorCode.FARM_FORBIDDEN;
import static com.barofarm.seller.farm.exception.FarmErrorCode.FARM_NOT_FOUND;
import static com.barofarm.seller.seller.exception.SellerErrorCode.SELLER_NOT_FOUND;

import com.barofarm.seller.common.exception.CustomException;
import com.barofarm.seller.common.response.CustomPage;
import com.barofarm.seller.common.response.ResponseDto;
import com.barofarm.seller.config.S3.S3Uploader;
import com.barofarm.seller.farm.application.dto.request.FarmCreateCommand;
import com.barofarm.seller.farm.application.dto.request.FarmUpdateCommand;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmListInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.domain.FarmImageRepository;
import com.barofarm.seller.farm.domain.FarmRepository;
import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.SellerRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmRepository farmRepository;
    private final SellerRepository sellerRepository;
    private final S3Uploader s3Uploader;
    private final FarmImageRepository farmImageRepository;

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
            S3Uploader.UploadedObject uploaded = null;
            try {
                uploaded = s3Uploader.uploadFarmImage(farm.getId(), image);
                farm.setImage(uploaded.url(), uploaded.key());
            } catch (Exception e) {
                if (uploaded != null) {
                    s3Uploader.deleteObject(uploaded.key());
                }
                throw e;
            }
        }

        Farm saved = farmRepository.save(farm);
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

        String oldKey = (farm.getImage() != null) ? farm.getImage().getS3Key() : null;

        if (image != null && !image.isEmpty()) {
            S3Uploader.UploadedObject uploaded = null;
            try {
                uploaded = s3Uploader.uploadFarmImage(farm.getId(), image);
                farm.setImage(uploaded.url(), uploaded.key());

                if (oldKey != null) {
                    s3Uploader.deleteObject(oldKey);
                }
            } catch (Exception e) {
                if (uploaded != null) {
                    s3Uploader.deleteObject(uploaded.key());
                }
                throw e;
            }
        } else {
            if (oldKey != null) {
                farm.removeImage();

                s3Uploader.deleteObject(oldKey);
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

        farmRepository.delete(farm);
        return ResponseDto.ok(null);
    }

    @Transactional(readOnly = true)
    public ResponseDto<CustomPage<FarmListInfo>> findFarmList(Pageable pageable) {
        Page<Farm> page = farmRepository.findAll(pageable);
        Page<FarmListInfo> dtoPage = page.map(FarmListInfo::from);
        return ResponseDto.ok(CustomPage.from(dtoPage));
    }
}
