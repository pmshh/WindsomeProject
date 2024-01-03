package com.windsome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.WithAccount;
import com.windsome.dto.CartItemDto;
import com.windsome.entity.Item;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.CartItemRepository;
import com.windsome.repository.CartRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.service.CartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired CartRepository cartRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired CartService cartService;

    @AfterEach
    void afterEach() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("장바구니 화면 보이는지 테스트")
    @WithAccount("test1234")
    public void cartHist() throws Exception {
        mockMvc.perform(get("/cart"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("cart/cartList"))
                .andExpect(model().attributeExists("cartItems"));
    }

    @Test
    @DisplayName("장바구니 아이템 추가 테스트")
    @WithAccount("test1234")
    public void addCart() throws Exception {
        Item item = saveItem();
        CartItemDto cartItemDto = createCartItemDto(item);
        Long cartItemId = cartService.addCart(cartItemDto, "test1234");

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(
                CartItemDto.builder().itemId(item.getId()).count(1).build());

        mockMvc.perform(post("/cart")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(cartItemId)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("장바구니 아이템 수정 테스트")
    @WithAccount("test1234")
    public void updateCartItem() throws Exception {
        Item item = saveItem();
        CartItemDto cartItemDto = createCartItemDto(item);
        Long cartItemId = cartService.addCart(cartItemDto, "test1234");

        mockMvc.perform(patch("/cartItem/" + cartItemId)
                        .param("count", String.valueOf(5))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(cartItemId)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("장바구니 아이템 취소 테스트")
    @WithAccount("test1234")
    public void deleteCartItem() throws Exception {
        Item item = saveItem();
        CartItemDto cartItemDto = createCartItemDto(item);
        Long cartItemId = cartService.addCart(cartItemDto, "test1234");

        mockMvc.perform(delete("/cartItem/" + cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(cartItemId)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    public Item saveItem() {
        Item item = Item.builder().itemNm("test").itemDetail("test").build();
        return itemRepository.save(item);
    }

    public CartItemDto createCartItemDto(Item item) {
        return CartItemDto.builder().itemId(item.getId()).count(1).build();
    }

}