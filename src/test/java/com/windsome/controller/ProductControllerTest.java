package com.windsome.controller;

import com.windsome.controller.advice.MemberControllerAdvice;
import com.windsome.controller.product.ProductController;
import com.windsome.dto.board.review.ProductReviewDTO;
import com.windsome.dto.product.ProductFormDTO;
import com.windsome.service.board.BoardService;
import com.windsome.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebMvcTest(ProductController.class)
@MockBean(JpaMetamodelMappingContext.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ProductControllerTest {

    @Autowired ProductController productController;

    @MockBean ProductService productService;
    @MockBean BoardService boardService;
    @MockBean MemberControllerAdvice memberControllerAdvice;

    @Test
    void testShowProductDetail() {
        // Given
        Long productId = 123L;
        Model mockModel = mock(Model.class);
        RedirectAttributes mockRedirectAttributes = mock(RedirectAttributes.class);

        ProductFormDTO productFormDTO = new ProductFormDTO();
        Pageable pageable = PageRequest.of(1, 5);
        Page<ProductReviewDTO> reviewPage = mock(Page.class);

        when(productService.getProductFormDto(productId)).thenReturn(productFormDTO);
        when(boardService.getProductReviewList(productId, pageable)).thenReturn(reviewPage);

        // When
        String viewName = productController.showProductDetail(Optional.of(1), productId, mockRedirectAttributes, mockModel);

        // Then
        assertEquals("main/product/product-detail", viewName);
        verify(mockModel).addAttribute(eq("product"), eq(productFormDTO));
        verify(mockModel).addAttribute(eq("reviews"), eq(reviewPage));
        verify(mockModel).addAttribute(eq("maxPage"), eq(5));
    }
}