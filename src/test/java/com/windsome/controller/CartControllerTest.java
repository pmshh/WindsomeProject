package com.windsome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.WithAccount;
import com.windsome.dto.cart.CartProductDTO;
import com.windsome.dto.cart.CartProductListDTO;
import com.windsome.entity.Color;
import com.windsome.entity.Size;
import com.windsome.entity.cart.Cart;
import com.windsome.entity.cart.CartProduct;
import com.windsome.entity.member.Member;
import com.windsome.entity.product.Product;
import com.windsome.repository.cartProduct.CartProductRepository;
import com.windsome.repository.cart.CartRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.product.ColorRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.product.SizeRepository;
import com.windsome.service.cart.CartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired CartProductRepository cartProductRepository;
    @Autowired CartService cartService;
    @Autowired ColorRepository colorRepository;
    @Autowired SizeRepository sizeRepository;

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

    @Test
    @DisplayName("장바구니 아이템 추가 테스트")
    @WithAccount("test1234")
    public void addCart() throws Exception {
        Product product = Product.builder().name("test").productDetail("test").price(10000).build();
        Product savedProduct = productRepository.save(product);

        Color color = new Color();
        color.setId(1L);
        Color savedColor = colorRepository.save(color);

        Size size = new Size();
        size.setId(1L);
        Size savedSize = sizeRepository.save(size);

        CartProductListDTO cartProductListDTO = new CartProductListDTO();
        cartProductListDTO.setProductId(savedProduct.getId());
        List<CartProductDTO> cartProductDTOList = new ArrayList<>();
        CartProductDTO cartProductDTO = new CartProductDTO();
        cartProductDTO.setColorId(savedColor.getId());
        cartProductDTO.setSizeId(savedSize.getId());
        cartProductDTO.setQuantity(1);
        cartProductDTOList.add(cartProductDTO);
        cartProductListDTO.setCartProductDTOList(cartProductDTOList);

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(cartProductListDTO);

        mockMvc.perform(post("/cart")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("장바구니에 상품이 추가되었습니다."))
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")));
    }

    @Test
    @DisplayName("장바구니 상품 개수 수정 테스트")
    @WithAccount("test1234")
    public void updateCartProductQuantity() throws Exception {
        Member member = memberRepository.findByUserIdentifier("test1234");

        // Color 생성 및 저장
        Color color = colorRepository.save(new Color());

        // Size 생성 및 저장
        Size size = sizeRepository.save(new Size());

        // Cart 생성 및 저장
        Cart cart = cartRepository.save(Cart.builder().member(member).build());

        // CartProduct 생성 및 저장
        CartProduct cartProduct = cartProductRepository.save(CartProduct.builder().cart(cart).build());

        mockMvc.perform(patch("/cart/" + cartProduct.getId())
                        .param("quantity", String.valueOf(5))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("장바구니 상품의 개수가 수정되었습니다."))
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")));
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트")
    @WithAccount("test1234")
    public void deleteCartProduct() throws Exception {
        Member member = memberRepository.findByUserIdentifier("test1234");

        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        for (int i = 1; i <= 3; i++) {
            CartProduct cartProduct = new CartProduct();
            cartProduct.setId((long) i);
            cartProduct.setCart(cart);
            cartProductRepository.save(cartProduct);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Long[] productIds = {1L, 2L, 3L};
        String jsonProductIds = objectMapper.writeValueAsString(productIds);

        mockMvc.perform(delete("/cart/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProductIds)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("장바구니 상품이 삭제되었습니다."))
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")));
    }

}