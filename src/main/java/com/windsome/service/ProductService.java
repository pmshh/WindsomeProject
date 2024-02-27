package com.windsome.service;

import com.windsome.dto.product.MainPageProductDTO;
import com.windsome.dto.product.ProductSearchDto;
import com.windsome.dto.product.ProductFormDto;
import com.windsome.dto.product.ProductImageDto;
import com.windsome.entity.*;
import com.windsome.exception.ProductImageDeletionException;
import com.windsome.repository.category.CategoryRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductImageService productImageService;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    @Value("${productImgLocation}")
    private String productImgLocation;

    /**
     * 상품 등록
     */
    public Long createProduct(ProductFormDto productFormDto, List<MultipartFile> productImageFileList) throws Exception {
        Category category = categoryRepository.findById(productFormDto.getCategoryId()).orElseThrow();
        Product product = productFormDto.toEntity();
        product.setCategory(category);
        productRepository.save(product);

        for (int i = 0; i < productImageFileList.size(); i++) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setRepresentativeImage(i == 0);

            productImageService.saveProductImage(productImage, productImageFileList.get(i));
        }
        return product.getId();
    }

    /**
     * 상품 수정
     */
    public Long updateProduct(ProductFormDto productFormDto, List<MultipartFile> productImageFileList) throws Exception {
        Category category = categoryRepository.findById(productFormDto.getCategoryId()).orElseThrow(EntityNotFoundException::new);
        Product product = productRepository.findById(productFormDto.getId()).orElseThrow(EntityNotFoundException::new);
        product.setCategory(category);
        product.updateProduct(productFormDto);

        List<Long> productImageIds = productFormDto.getProductImageIds();

        for (int i = 0; i < productImageFileList.size(); i++) {
            productImageService.updateProductImage(productImageIds.get(i), productImageFileList.get(i));
        }
        return product.getId();
    }

    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductFormDto getProductFormDto(Long productId) {
        List<ProductImage> productImages = productImageRepository.findByProductIdOrderByIdAsc(productId);

        List<ProductImageDto> productImageDtoList = new ArrayList<>();
        for (ProductImage productImage : productImages) {
            ProductImageDto productImageDto = ProductImageDto.toDto(productImage);
            productImageDtoList.add(productImageDto);
        }

        Product product = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        ProductFormDto productFormDto = ProductFormDto.toDto(product);
        productFormDto.setCategoryId(product.getCategory().getId());
        productFormDto.setProductImageDtoList(productImageDtoList);
        return productFormDto;
    }

    /**
     * 메인 화면 상품 리스트 조회
     */
    @Transactional(readOnly = true)
    public Page<MainPageProductDTO> getMainPageProducts(ProductSearchDto productSearchDto, Pageable pageable) {
        return productRepository.getMainPageProducts(productSearchDto, pageable);
    }

    /**
     * 상품 삭제
     */
    public void deleteProduct(Long productId) throws Exception {
        List<ProductImage> productImageList = productImageRepository.findByProductId(productId).orElseThrow(EntityNotFoundException::new);
        Product product = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);

        productImageRepository.deleteAll(productImageList);
        productRepository.delete(product);

        for (ProductImage productImage : productImageList) {
            // 기존 이미지 삭제
            if (!StringUtils.isEmpty(productImage.getServerImageName())) {
                fileService.deleteFile(productImgLocation + "/" + productImage.getServerImageName());
            }
        }
    }

    /**
     * 상품 이미지 삭제
     */
    public void deleteProductImage(Long productImageId) {
        ProductImage productImage = productImageRepository.findById(productImageId).orElseThrow(EntityNotFoundException::new);
        if (!productImage.isRepresentativeImage()) {
            productImage.setServerImageName("");
            productImage.setImageUrl("");
            productImage.setOriginalImageName("");
            productImageRepository.save(productImage);
        } else {
            throw new ProductImageDeletionException("첫 번째 상품 이미지는 삭제할 수 없습니다.");
        }
    }
}
