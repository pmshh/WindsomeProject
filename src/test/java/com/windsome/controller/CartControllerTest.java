package com.windsome.controller;

import com.windsome.WithAccount;
import com.windsome.dto.cart.CartProductDto;
import com.windsome.entity.Product;
import com.windsome.repository.cartProduct.CartProductRepository;
import com.windsome.repository.cart.CartRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.service.CartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired CartProductRepository cartProductRepository;
    @Autowired CartService cartService;

    @AfterEach
    void afterEach() {
        cartProductRepository.deleteAll();
        cartRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("장바구니 화면 보이는지 테스트")
    @WithAccount("test1234")
    public void cartHist() throws Exception {
        mockMvc.perform(get("/cart"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("cart/cart-list"))
                .andExpect(model().attributeExists("cartProducts"));
    }

//    @Test
//    @DisplayName("장바구니 아이템 추가 테스트")
//    @WithAccount("test1234")
//    public void addCart() throws Exception {
//        Product product = saveProduct();
//        CartProductDto cartProductDto = createCartProductDto(product);
//        Long cartItemId = cartService.addCartProduct(cartProductDto, "test1234");
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        String content = objectMapper.writeValueAsString(
//                CartProductDto.builder().productId(product.getId()).count(1).build());
//
//        mockMvc.perform(post("/cart")
//                        .content(content)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .with(csrf()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string(String.valueOf(cartItemId)))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }

//    @Test
//    @DisplayName("장바구니 아이템 수정 테스트")
//    @WithAccount("test1234")
//    public void updateCartItem() throws Exception {
//        Product product = saveProduct();
//        CartProductDto cartProductDto = createCartProductDto(product);
//        Long cartItemId = cartService.addCartProduct(cartProductDto, "test1234");
//
//        mockMvc.perform(patch("/cartItem/" + cartItemId)
//                        .param("count", String.valueOf(5))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .with(csrf()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string(String.valueOf(cartItemId)))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }

//    @Test
//    @DisplayName("장바구니 아이템 취소 테스트")
//    @WithAccount("test1234")
//    public void deleteCartItem() throws Exception {
//        Product product = saveProduct();
//        CartProductDto cartProductDto = createCartProductDto(product);
//        Long cartItemId = cartService.addCartProduct(cartProductDto, "test1234");
//
//        mockMvc.perform(delete("/cartItem/" + cartItemId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .with(csrf()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string(String.valueOf(cartItemId)))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }

    public Product saveProduct() {
        Product product = Product.builder().name("test").productDetail("test").build();
        return productRepository.save(product);
    }

    public CartProductDto createCartProductDto(Product product) {
        return CartProductDto.builder().productId(product.getId()).count(1).build();
    }

}