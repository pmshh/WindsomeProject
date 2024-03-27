package com.windsome.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.WithAccount;
import com.windsome.dto.admin.PageDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.dto.product.ProductFormDTO;
import com.windsome.exception.ProductImageDeletionException;
import com.windsome.service.admin.AdminService;
import com.windsome.service.product.CategoryService;
import com.windsome.service.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(AdminProductController.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@MockBean(JpaMetamodelMappingContext.class)
@Transactional
class AdminProductControllerTest {

    @MockBean ProductService productService;
    @MockBean AdminService adminService;
    @MockBean CategoryService categoryService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("상품 조회 기능 테스트")
    @WithAccount("ADMIN")
    void getItemList() throws Exception {
        given(adminService.getProductList(any(ProductSearchDTO.class), any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/product-management"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("productSearchDto"))
                .andExpect(model().attributeExists("maxPage"));
    }

    @Test
    @DisplayName("상품 등록 화면 테스트")
    @WithAccount("ADMIN")
    void showItemFormTest() throws Exception {
        mockMvc.perform(get("/admin/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/product-create"))
                .andExpect(model().attributeExists("productFormDto"));
    }

    @Test
    @DisplayName("상품 등록 테스트")
    @WithAccount("ADMIN")
    void enrollItemTest() throws Exception {
        // Mocking
        given(productService.createProduct(any(ProductFormDTO.class), anyList())).willReturn(1L);

        // Creating MockMultipartFile
        MockMultipartFile mockMultipartFile = new MockMultipartFile("productImageFile", "test.jpg", "image/jpeg", "test image content".getBytes());

        // Performing request
        mockMvc.perform(multipart("/admin/products/new")
                        .file(mockMultipartFile)
                        .param("name", "Test 상품명")
                        .param("price", "10000")
                        .param("discount", "0")
                        .param("productDetail", "Test 상품 상세")
                        .param("stockNumber", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "상품이 등록되었습니다."));
    }

    @Test
    @DisplayName("상품 상세 화면 테스트")
    @WithAccount("ADMIN")
    void viewItemDetailTest() throws Exception {
        // Mocking
        Long productId = 1L;
        ProductFormDTO productFormDto = new ProductFormDTO();
        PageDTO pageDto = new PageDTO();
        given(productService.getProductFormDto(productId)).willReturn(productFormDto);

        // Performing request
        mockMvc.perform(get("/admin/products/{productId}", productId)
                        .param("page", "0")
                        .param("searchDateType", "")
                        .param("searchBy", "")
                        .param("searchQuery", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/product-form"))
                .andExpect(model().attributeExists("type"))
                .andExpect(model().attribute("type", "detail"))
                .andExpect(model().attributeExists("productFormDto"))
                .andExpect(model().attributeExists("pageDto"));
    }

    @Test
    @DisplayName("상품 수정 화면 테스트")
    @WithAccount("ADMIN")
    void showUpdateItemFormTest() throws Exception {
        // Mocking
        Long productId = 1L;
        ProductFormDTO productFormDto = new ProductFormDTO();
        PageDTO pageDto = new PageDTO();
        given(productService.getProductFormDto(productId)).willReturn(productFormDto);

        // Performing request
        mockMvc.perform(get("/admin/products/{productId}/edit", productId)
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/product-form"))
                .andExpect(model().attributeExists("type"))
                .andExpect(model().attribute("type", "update"))
                .andExpect(model().attributeExists("productFormDto"))
                .andExpect(model().attributeExists("pageDto"));
    }

    @Test
    @DisplayName("상품 수정 테스트")
    @WithAccount("ADMIN")
    void updateItemTest() throws Exception {
        // Mocking
        given(productService.updateProduct(any(ProductFormDTO.class), anyList())).willReturn(1L);

        // Creating MockMultipartFile
        MockMultipartFile mockMultipartFile = new MockMultipartFile("productImageFile", "test.jpg", "image/jpeg", "test image content".getBytes());

        // Performing request
        mockMvc.perform(multipart("/admin/products/{productId}", 1L)
                        .file(mockMultipartFile)
                        .param("name", "test")
                        .param("price", "10000")
                        .param("productDetail", "Test 상세 설명")
                        .param("stockNumber", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "상품을 수정했습니다."));
    }

    @Test
    @DisplayName("상품 이미지 삭제 성공 테스트")
    @WithAccount("ADMIN")
    void deleteItemImageSuccessTest() throws Exception {
        // Mocking - 이미지 삭제가 성공하는 경우
        doNothing().when(productService).deleteProductImage(anyLong());

        // Perform & Verify
        mockMvc.perform(patch("/admin/products/{productImageId}", 123L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("이미지가 삭제되었습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("상품 이미지 삭제 실패 - 존재하지 않는 이미지")
    @WithAccount("ADMIN")
    void deleteItemImageFailureNotFoundTest() throws Exception {
        // Mocking - 존재하지 않는 이미지 삭제 시 EntityNotFoundException이 발생하는 경우
        doThrow(EntityNotFoundException.class).when(productService).deleteProductImage(anyLong());

        // Perform & Verify
        mockMvc.perform(patch("/admin/products/{productImageId}", 123L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("존재하지 않는 이미지입니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("상품 이미지 삭제 실패 - 이미지 삭제 중 에러 발생")
    @WithAccount("ADMIN")
    void deleteItemImageFailureErrorTest() throws Exception {
        // Mocking - 이미지 삭제 중 에러가 발생하는 경우
        String errorMessage = "이미지 삭제 중 에러가 발생했습니다.";
        doThrow(new ProductImageDeletionException(errorMessage)).when(productService).deleteProductImage(anyLong());

        // Perform & Verify
        mockMvc.perform(patch("/admin/products/{productImageId}", 123L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes(errorMessage.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("상품 삭제 테스트 - 성공")
    @WithAccount("ADMIN")
    void deleteItemSuccessTest() throws Exception {
        // Mocking - 상품 삭제가 성공하는 경우
        Long[] productIds = {1L, 2L, 3L};
        doNothing().when(productService).deleteProducts(any());

        // productIds 배열을 JSON 문자열로 변환
        String jsonProductIds = new ObjectMapper().writeValueAsString(productIds);

        // Perform & Verify
        mockMvc.perform(delete("/admin/products/delete")
                        .content(jsonProductIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("상품이 삭제되었습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("상품 삭제 테스트 - 실패")
    @WithAccount("ADMIN")
    void deleteItemFailureTest() throws Exception {
        // Mocking - 일치하는 상품 정보가 없는 경우
        Long[] productIds = {1L, 2L, 3L};
        doThrow(new Exception()).when(productService).deleteProducts(any());

        // productIds 배열을 JSON 문자열로 변환
        String jsonProductIds = new ObjectMapper().writeValueAsString(productIds);

        // Perform & Verify
        mockMvc.perform(delete("/admin/products/delete")
                        .content(jsonProductIds)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("일치하는 상품 정보가 없습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("카테고리 조회 테스트")
    @WithAccount("ADMIN")
    void getItemCategoriesTest() throws Exception {
        // Perform & Verify
        mockMvc.perform(get("/admin/products/categories").with(csrf()))
                .andExpect(status().isOk());
    }

}