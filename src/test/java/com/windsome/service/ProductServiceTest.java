package com.windsome.service;

import com.windsome.dto.product.MainPageProductDTO;
import com.windsome.dto.product.ProductFormDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.entity.Category;
import com.windsome.entity.Product;
import com.windsome.entity.ProductImage;
import com.windsome.exception.ProductImageDeletionException;
import com.windsome.repository.category.CategoryRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductImageService productImageService;
    @Mock private CategoryRepository categoryRepository;
    @Mock private FileService fileService;

    @Value("${productImgLocation}")
    private String productImgLocation;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 등록 - productImageFileList가 파일을 포함하는 경우")
    void testCreateProduct_WithFiles() throws Exception {
        // Given
        ProductFormDTO productFormDto = new ProductFormDTO();
        productFormDto.setId(1L);
        productFormDto.setCategoryId(1L);

        Category category = new Category();
        category.setId(1L);

        List<MultipartFile> productImageFileList = new ArrayList<>();
        productImageFileList.add(new MockMultipartFile("file1.jpg", "file1.jpg", "image/jpeg", "file1".getBytes()));
        productImageFileList.add(new MockMultipartFile("file2.jpg", "file2.jpg", "image/jpeg", "file2".getBytes()));

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        // When
        Long productId = productService.createProduct(productFormDto, productImageFileList);

        // Then
        assertNotNull(productId);
        verify(productRepository, times(1)).save(any()); // Product 저장이 한 번 호출되었는지 확인
        verify(productImageService, times(productImageFileList.size())).saveProductImage(any(), any()); // ProductImageService의 saveProductImage가 파일 개수만큼 호출되었는지 확인
    }

    @Test
    @DisplayName("상품 등록 - productImageFileList가 파일을 포함하지 않는 경우")
    void testCreateProduct_NoFiles() throws Exception {
        // Given
        ProductFormDTO productFormDto = new ProductFormDTO();
        productFormDto.setId(1L);
        productFormDto.setCategoryId(1L);

        Category category = new Category();
        category.setId(1L);

        List<MultipartFile> productImageFileList = Collections.emptyList();

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        // When
        Long productId = productService.createProduct(productFormDto, productImageFileList);

        // Then
        assertNotNull(productId);
        verify(productRepository, times(1)).save(any());
        verify(productImageService, never()).saveProductImage(any(), any());
    }

    @Test
    @DisplayName("상품 수정 - productImageFileList가 파일을 포함하는 경우")
    void testUpdateProduct_WithFiles() throws Exception {
        // Given
        ProductFormDTO productFormDto = new ProductFormDTO();
        productFormDto.setId(1L);
        productFormDto.setProductName("Test Name");
        productFormDto.setPrice(10000);
        productFormDto.setStockNumber(100);
        productFormDto.setCategoryId(1L);
        List<Long> productImageIds = new ArrayList<>();
        productImageIds.add(1L);
        productImageIds.add(2L);
        productFormDto.setProductImageIds(productImageIds);

        List<MultipartFile> productImageFileList = new ArrayList<>();
        // 파일 추가
        productImageFileList.add(new MockMultipartFile("file1.jpg", "file1.jpg", "image/jpeg", "file1".getBytes()));
        productImageFileList.add(new MockMultipartFile("file2.jpg", "file2.jpg", "image/jpeg", "file2".getBytes()));

        // Product, Category 조회 결과 설정
        Product product = new Product();
        product.setId(1L);
        Category category = new Category();
        category.setId(1L);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        doNothing().when(productImageService).updateProductImage(anyLong(), any());

        // When
        Long productId = productService.updateProduct(productFormDto, productImageFileList);

        // Then
        assertNotNull(productId);
        assertEquals(product.getPrice(), productFormDto.getPrice());
        verify(productRepository, times(1)).findById(anyLong());
        verify(categoryRepository, times(1)).findById(anyLong());
        verify(productImageService, times(productFormDto.getProductImageIds().size())).updateProductImage(anyLong(), any());
    }

    @Test
    @DisplayName("상품 수정 - productImageFileList가 파일을 포함하지 않는 경우")
    void testUpdateProduct_NoFiles() throws Exception {
        // Given
        ProductFormDTO productFormDto = new ProductFormDTO();
        productFormDto.setId(1L);
        productFormDto.setProductName("Test Name");
        productFormDto.setPrice(10000);
        productFormDto.setStockNumber(100);
        productFormDto.setCategoryId(1L);

        List<MultipartFile> productImageFileList = new ArrayList<>();

        // Product, Category 조회 결과 설정
        Product product = new Product();
        product.setId(1L);
        Category category = new Category();
        category.setId(1L);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        // When
        Long productId = productService.updateProduct(productFormDto, productImageFileList);

        // Then
        assertNotNull(productId);
        assertEquals(product.getPrice(), productFormDto.getPrice());
        verify(productRepository, times(1)).findById(anyLong());
        verify(categoryRepository, times(1)).findById(anyLong());
        verify(productImageService, times(productFormDto.getProductImageIds().size())).updateProductImage(anyLong(), any());
    }

    @Test
    @DisplayName("상품 상세 조회 - 해당 상품이 존재하는 경우")
    void testGetProductFormDto_ProductExists() {
        // Given
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setCategory(new Category());
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.findByProductIdOrderByIdAsc(productId)).thenReturn(new ArrayList<>());

        // When
        ProductFormDTO productFormDto = productService.getProductFormDto(productId);

        // Then
        assertEquals(productId, productFormDto.getId());
    }

    @Test
    @DisplayName("상품 상세 조회 - 해당 상품이 존재하지 않는 경우")
    void testGetProductFormDto_ProductNotExists() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.getProductFormDto(productId));
    }

    @Test
    @DisplayName("메인 화면 상품 리스트 조회 - 상품이 존재하는 경우")
    void testGetMainPageProducts_ProductExists() {
        // Given
        ProductSearchDTO productSearchDto = new ProductSearchDTO();
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        Page<MainPageProductDTO> expectedPage = new PageImpl<>(Collections.singletonList(new MainPageProductDTO()));

        when(productRepository.getMainPageProducts(productSearchDto, pageable)).thenReturn(expectedPage);

        // When
        Page<MainPageProductDTO> resultPage = productService.getMainPageProducts(productSearchDto, pageable);

        // Then
        assertEquals(expectedPage, resultPage);
    }

    @Test
    @DisplayName("메인 화면 상품 리스트 조회 - 상품이 존재하지 않는 경우")
    void testGetMainPageProducts_ProductNotExists() {
        // Given
        ProductSearchDTO productSearchDto = new ProductSearchDTO();
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        Page<MainPageProductDTO> emptyPage = Page.empty();

        when(productRepository.getMainPageProducts(productSearchDto, pageable)).thenReturn(emptyPage);

        // When
        Page<MainPageProductDTO> resultPage = productService.getMainPageProducts(productSearchDto, pageable);

        // Then
        assertEquals(emptyPage, resultPage);
    }

    @Test
    @DisplayName("상품 삭제 - 상품 및 상품 이미지가 존재하는 경우")
    void testDeleteProduct_ProductAndProductImageExist() throws Exception {
        // Given
        Long productId = 1L;
        List<ProductImage> productImageList = new ArrayList<>();
        ProductImage productImage = new ProductImage();
        productImage.setServerImageName("Test Name");
        productImageList.add(productImage);
        Product product = new Product();

        when(productImageRepository.findByProductId(productId)).thenReturn(Optional.of(productImageList));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        assertDoesNotThrow(() -> productService.deleteProduct(productId));

        // Then
        verify(productRepository, times(1)).delete(product);
        verify(productImageRepository, times(1)).deleteAll(productImageList);
        verify(fileService, times(productImageList.size())).deleteFile(anyString());
    }

    @Test
    @DisplayName("상품 삭제 - 존재하지 않는 상품 삭제")
    void testDeleteProduct_NonExistingProduct() {
        // Given
        Long productId = 1L;

        // When, Then
        assertThrows(EntityNotFoundException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    @DisplayName("상품 이미지 삭제 - 대표 이미지가 아닌 경우")
    void testDeleteProductImage_NotRepresentativeImage() {
        // Given
        Long productImageId = 1L;
        ProductImage productImage = new ProductImage();
        productImage.setRepresentativeImage(false);
        when(productImageRepository.findById(productImageId)).thenReturn(Optional.of(productImage));

        // When
        assertDoesNotThrow(() -> productService.deleteProductImage(productImageId));

        // Then
        verify(productImageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("상품 이미지 삭제 - 대표 이미지인 경우")
    void testDeleteProductImage_RepresentativeImage() {
        // Given
        Long productImageId = 1L;
        ProductImage productImage = new ProductImage();
        productImage.setRepresentativeImage(true);
        when(productImageRepository.findById(productImageId)).thenReturn(Optional.of(productImage));

        // When, Then
        assertThrows(ProductImageDeletionException.class, () -> productService.deleteProductImage(productImageId));
    }

    @Test
    @DisplayName("상품 이미지 삭제 - 존재하지 않는 상품 이미지 삭제 시도")
    void testDeleteProductImage_NonExistingProductImage() {
        // Given
        Long productImageId = 1L;
        when(productImageRepository.findById(productImageId)).thenReturn(Optional.empty());

        // When, Then
        assertThrows(EntityNotFoundException.class, () -> productService.deleteProductImage(productImageId));
    }
}