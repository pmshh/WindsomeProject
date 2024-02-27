package com.windsome.controller;

import com.windsome.constant.ProductSellStatus;
import com.windsome.dto.product.ProductFormDto;
import com.windsome.entity.Category;
import com.windsome.repository.category.CategoryRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ProductRepository productRepository;
    @Autowired ProductImageRepository productImageRepository;
    @Autowired ProductService productService;
    @Autowired CategoryRepository categoryRepository;

    @Test
    @DisplayName("아이템 상세 화면 보이는지 테스트")
    public void cartHist() throws Exception {
        Category category = new Category();
        categoryRepository.save(category);

        ProductFormDto productFormDto = getProductFormDto();
        productFormDto.setCategoryId(category.getId());

        List<MultipartFile> multipartFiles = createMultipartFiles();

        Long productId = productService.createProduct(productFormDto, multipartFiles);

        mockMvc.perform(get("/product/" + productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("main/product/product-detail"))
                .andExpect(model().attributeExists("product"));
    }

    private ProductFormDto getProductFormDto() {
        return new ProductFormDto(null, "test", 10000, 0.0, "test", 100, ProductSellStatus.SELL, null, null, null);
    }

    List<MultipartFile> createMultipartFiles() throws Exception {
        List<MultipartFile> multipartFileList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String path = "C:/shop/item/";
            String imageName = "imageName" + i + ".jpg";
            MockMultipartFile multipartFile = new MockMultipartFile(path, imageName, "image/jpg", new byte[]{1, 2, 3, 4});
            multipartFileList.add(multipartFile);
        }
        return multipartFileList;
    }
}