package com.barofarm.buyer.product.application;

import com.barofarm.buyer.product.application.dto.ProductImageUpdateMode;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductImage;
import com.barofarm.storage.s3.S3ImageUploader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final S3ImageUploader s3ImageUploader;

    public void createProductImagesSafely(Product product, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        List<S3ImageUploader.UploadedImage> uploadedImages = uploadImages(images);
        replaceProductImages(product, uploadedImages);
    }

    public void updateProductImagesSafely(
        Product product,
        List<MultipartFile> images,
        ProductImageUpdateMode imageUpdateMode
    ) {
        if (imageUpdateMode == null || imageUpdateMode == ProductImageUpdateMode.KEEP) {
            return;
        }

        if (imageUpdateMode == ProductImageUpdateMode.CLEAR) {
            deleteProductImagesSafely(product);
            return;
        }

        if (images != null && images.isEmpty()) {
            deleteProductImagesSafely(product);
            return;
        }

        List<String> oldKeys = product.getImages()
            .stream()
            .map(ProductImage::getS3Key)
            .filter(key -> key != null && !key.isBlank())
            .toList();

        List<S3ImageUploader.UploadedImage> uploadedImages = uploadImages(images);
        replaceProductImages(product, uploadedImages);
        cleanupUploadedImagesByKeys(oldKeys);
    }

    public void deleteProductImagesSafely(Product product) {
        List<String> imageKeys = product.getImages()
            .stream()
            .map(ProductImage::getS3Key)
            .filter(key -> key != null && !key.isBlank())
            .toList();
        product.clearImages();
        cleanupUploadedImagesByKeys(imageKeys);
    }

    private List<S3ImageUploader.UploadedImage> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        for (MultipartFile image : images) {
            if (image == null) {
                throw new IllegalArgumentException("Image file이 존재하지 않습니다");
            }
        }

        return images.stream()
            .map(image -> s3ImageUploader.uploadWebpImage("product", image))
            .toList();
    }

    private void replaceProductImages(Product product, List<S3ImageUploader.UploadedImage> uploadedImages) {
        if (product == null) {
            throw new IllegalArgumentException("product가 null입니다");
        }
        if (uploadedImages == null || uploadedImages.isEmpty()) {
            return;
        }

        product.clearImages();
        int sortOrder = 0;
        for (S3ImageUploader.UploadedImage uploaded : uploadedImages) {
            product.addImage(uploaded.url(), uploaded.key(), sortOrder++);
        }
    }

    private void cleanupUploadedImagesByKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            s3ImageUploader.deleteObject(key);
        }
    }
}
