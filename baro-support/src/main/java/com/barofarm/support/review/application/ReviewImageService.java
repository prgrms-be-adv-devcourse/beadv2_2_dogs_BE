package com.barofarm.support.review.application;

import com.barofarm.storage.s3.S3ImageUploader;
import com.barofarm.support.review.application.dto.request.ReviewImageUpdateMode;
import com.barofarm.support.review.domain.Review;
import com.barofarm.support.review.domain.ReviewImage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewImageService {

    private final S3ImageUploader s3ImageUploader;

    public void createReviewImagesSafely(
        Review review,
        List<MultipartFile> images,
        UUID productId,
        UUID orderItemId,
        UUID userId
    ) {
        if (images == null || images.isEmpty()) {
            return;
        }

        List<S3ImageUploader.UploadedImage> uploadedImages = List.of();
        try {
            // 1. 이미지 업로드
            uploadedImages = uploadImages(images);

            // 2. 연관관계 교체
            replaceReviewImages(review, uploadedImages);
        } catch (Exception e) {
            log.warn(
                "리뷰 이미지 업로드에 실패하여 이미지 없이 리뷰를 생성합니다. productId={}, orderItemId={}, userId={}, 요청이미지수={}, 업로드된키={}",
                productId,
                orderItemId,
                userId,
                images.size(),
                uploadedImages.stream().map(S3ImageUploader.UploadedImage::key).toList(),
                e
            );
            cleanupUploadedImagesSafely(uploadedImages, productId, orderItemId, userId);
            review.clearImages();
        }
    }

    public void updateReviewImagesSafely(
        Review review,
        List<MultipartFile> images,
        ReviewImageUpdateMode imageUpdateMode,
        UUID userId
    ) {
        // null이거나 유지 정책일 경우 KEEP
        if (imageUpdateMode == null || imageUpdateMode == ReviewImageUpdateMode.KEEP) {
            return;
        }
        // CLEAR 정책일 경우 모두 삭제
        if (imageUpdateMode == ReviewImageUpdateMode.CLEAR) {
            deleteReviewImagesSafely(review, userId);
            return;
        }
        // null이 아닌데 Empty일 경우 모두 삭제
        if (images != null && images.isEmpty()) {
            deleteReviewImagesSafely(review, userId);
            return;
        }

        List<String> oldKeys = review.getImages()
            .stream()
            .map(ReviewImage::getS3Key)
            .toList();
        List<S3ImageUploader.UploadedImage> uploadedImages = List.of();
        try {
            // 1) 새 이미지 업로드를 먼저 시도한다(기존 이미지/리뷰 상태를 건드리지 않음)
            if (images == null) {
                throw new IllegalArgumentException("imageUpdateMode가 REPLACE일 때 images는 필수입니다");
            }
            uploadedImages = uploadImages(images);

            // 2) 업로드가 모두 성공했을 때만 DB 연관관계를 교체한다
            replaceReviewImages(review, uploadedImages);

            // 3) 기존 S3 객체 정리(실패해도 수정 흐름은 막지 않음)
            cleanupUploadedImagesByKeysSafely(oldKeys, review.getProductId(), review.getId(), userId);
        } catch (Exception e) {
            log.warn(
                "리뷰 수정 중 이미지 업로드에 실패하여 기존 이미지를 유지합니다. (리뷰 수정은 정상 처리됨) "
                    + "productId={}, reviewId={}, userId={}, 요청이미지수={}, 업로드된키={}",
                review.getProductId(),
                review.getId(),
                userId,
                images == null ? 0 : images.size(),
                uploadedImages.stream().map(S3ImageUploader.UploadedImage::key).toList(),
                e
            );
            cleanupUploadedImagesSafely(uploadedImages, review.getProductId(), review.getOrderItemId(), userId);
        }
    }

    public void deleteReviewImagesSafely(Review review, UUID userId) {
        List<String> imageKeys = review.getImages()
            .stream()
            .map(ReviewImage::getS3Key)
            .toList();
        review.clearImages();
        cleanupUploadedImagesByKeysSafely(imageKeys, review.getProductId(), review.getId(), userId);
    }

    public List<S3ImageUploader.UploadedImage> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        for (MultipartFile image : images) {
            if (image == null) {
                throw new IllegalArgumentException("Image file이 존재하지 않습니다");
            }
        }

        return images.stream()
            .map(image -> s3ImageUploader.uploadWebpImage("review", image))
            .toList();
    }

    public void replaceReviewImages(Review review, List<S3ImageUploader.UploadedImage> uploadedImages) {
        if (review == null) {
            throw new IllegalArgumentException("review가 null입니다");
        }
        if (uploadedImages == null || uploadedImages.isEmpty()) {
            return;
        }

        review.clearImages();
        int sortOrder = 0;
        for (S3ImageUploader.UploadedImage uploaded : uploadedImages) {
            review.addImage(uploaded.url(), uploaded.key(), sortOrder++);
        }
    }

    public void cleanupUploadedImagesSafely(
        List<S3ImageUploader.UploadedImage> uploadedImages,
        UUID productId,
        UUID orderItemId,
        UUID userId
    ) {
        if (uploadedImages == null || uploadedImages.isEmpty()) {
            return;
        }

        try {
            cleanupUploadedImages(uploadedImages);
        } catch (Exception cleanupEx) {
            log.error(
                "이미지 업로드 실패 후 S3 정리(cleanup)에 실패했습니다. 수동 정리가 필요할 수 있습니다. "
                    + "productId={}, orderItemId={}, userId={}, 남은키={}",
                productId,
                orderItemId,
                userId,
                uploadedImages.stream().map(S3ImageUploader.UploadedImage::key).toList(),
                cleanupEx
            );
        }
    }

    public void cleanupUploadedImagesByKeysSafely(
        List<String> keys,
        UUID productId,
        UUID reviewId,
        UUID userId
    ) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        try {
            cleanupUploadedImagesByKeys(keys);
        } catch (Exception e) {
            log.error(
                "S3 이미지 삭제(cleanup)에 실패했습니다. 수동 정리가 필요할 수 있습니다. productId={}, reviewId={}, userId={}, 남은키={}",
                productId,
                reviewId,
                userId,
                keys,
                e
            );
        }
    }

    public void cleanupUploadedImages(List<S3ImageUploader.UploadedImage> uploadedImages) {
        if (uploadedImages == null || uploadedImages.isEmpty()) {
            return;
        }

        for (S3ImageUploader.UploadedImage uploaded : uploadedImages) {
            s3ImageUploader.deleteObject(uploaded.key());
        }
    }

    public void cleanupUploadedImagesByKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            s3ImageUploader.deleteObject(key);
        }
    }
}
