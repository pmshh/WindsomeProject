package com.windsome.service;

import com.windsome.entity.product.ProductImage;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.service.product.ProductImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock private ProductImageRepository productImageRepository;
    @Mock private FileService fileService;

    @Value("${productImgLocation}")
    private String productImgLocation;

    @InjectMocks private ProductImageService productImageService;

    @Test
    @DisplayName("제품 이미지 저장 - 성공적인 경우")
    void testSaveProductImage_Success() throws Exception {
        // Given
        String originalImageName = "test.jpg";
        MultipartFile multipartFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());
        ProductImage productImage = new ProductImage();
        when(fileService.uploadFile(productImgLocation, originalImageName, multipartFile.getBytes())).thenReturn("test.jpg");

        // When
        productImageService.saveProductImage(productImage, multipartFile);

        // Then
        verify(fileService, times(1)).uploadFile(productImgLocation, originalImageName, multipartFile.getBytes());
        verify(productImageRepository, times(1)).save(productImage);
    }

    @Test
    @DisplayName("이미지 수정 - 이미지 파일이 비어있을 때")
    void testUpdateProductImage_EmptyFile() throws Exception {
        // Given
        Long productImageId = 1L;
        MultipartFile productImageFile = mock(MultipartFile.class);
        when(productImageFile.isEmpty()).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productImageService.updateProductImage(productImageId, productImageFile));

        // Then
        verify(productImageRepository, never()).findById(productImageId);
        verify(fileService, never()).deleteFile(any());
        verify(fileService, never()).uploadFile(any(), any(), any());
    }

    @Test
    @DisplayName("이미지 수정 - 파일이 비어있지 않은 경우")
    void testUpdateProductImage_NonEmptyFile() throws Exception {
        // Given
        Long productImageId = 1L;
        String originalImageName = "test.jpg";
        String serverImageName = "serverImageName.jpg";
        MultipartFile productImageFile = mock(MultipartFile.class);

        ProductImage savedProductImage = new ProductImage();
        when(productImageFile.isEmpty()).thenReturn(false);
        when(productImageRepository.findById(productImageId)).thenReturn(Optional.of(savedProductImage));
        when(productImageFile.getOriginalFilename()).thenReturn(originalImageName); // "test.jpg" 반환
        when(fileService.uploadFile(productImgLocation, originalImageName, productImageFile.getBytes())).thenReturn(serverImageName);

        // When
        assertDoesNotThrow(() -> productImageService.updateProductImage(productImageId, productImageFile));

        // Then
        assertEquals(savedProductImage.getOriginalImageName(), originalImageName);
        assertEquals(savedProductImage.getServerImageName(), serverImageName);
        assertEquals(savedProductImage.getImageUrl(), "/images/product/" + serverImageName);
    }

}