package com.windsome.service.product;

import com.windsome.entity.product.ProductImage;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {

    @Value("${productImgLocation}")
    private String productImgLocation;

    private final ProductImageRepository productImageRepository;

    private final FileService fileService;

    /**
     * Entity 저장
     */
    public void save(ProductImage productImage) {
        productImageRepository.save(productImage);
    }

    /**
     * 이미지 단건 조회
     */
    public ProductImage getProductImageByProductImageId(Long productImageId) {
        return productImageRepository.findById(productImageId).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 이미지 List 조회
     */
    public List<ProductImage> getProductImagesByProductId(Long productId) {
        return productImageRepository.findAllByProductIdOrderByIdAsc(productId);
    }

    /**
     * 대표 이미지 url 조회
     */
    public String getRepresentativeImageUrl(Long productId, boolean isRepresentative) {
        return productImageRepository.findByProductIdAndIsRepresentativeImage(productId, isRepresentative).getImageUrl();
    }

    /**
     * 이미지 등록
     */
    public void saveProductImage(ProductImage productImage, MultipartFile productImageFile) throws Exception {
        String originalImageName = productImageFile.getOriginalFilename();
        String serverImageName = "";
        String imageUrl = "";

        // 파일 업로드
        if (!StringUtils.isEmpty(originalImageName)) {
            serverImageName = fileService.uploadFile(productImgLocation, originalImageName, productImageFile.getBytes());
            imageUrl = "/images/product/" + serverImageName;
        }

        // 이미지 정보 DB 업데이트
        productImage.updateProductImage(originalImageName, serverImageName, imageUrl);
        productImageRepository.save(productImage);
    }

    /**
     * 이미지 수정
     */
    public void updateProductImage(Long productImageId, MultipartFile productImageFile) throws Exception {
        if (!productImageFile.isEmpty()) {
            ProductImage savedProductImage = productImageRepository.findById(productImageId).orElseThrow(EntityNotFoundException::new); // itemImg 조회

            // 기존 이미지 삭제
            if (!StringUtils.isEmpty(savedProductImage.getServerImageName())) {
                fileService.deleteFile(productImgLocation + "/" + savedProductImage.getServerImageName());
            }

            String originalImageName = productImageFile.getOriginalFilename();
            // 새 이미지 서버에 업로드
            String serverImageName = fileService.uploadFile(productImgLocation, originalImageName, productImageFile.getBytes());
            String imageUrl = "/images/product/" + serverImageName;

            // 이미지 정보 DB 업데이트
            savedProductImage.updateProductImage(originalImageName, serverImageName, imageUrl);
        }
    }
}
