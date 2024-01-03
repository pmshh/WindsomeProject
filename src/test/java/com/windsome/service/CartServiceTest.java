package com.windsome.service;

import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.CartDetailDto;
import com.windsome.dto.CartItemDto;
import com.windsome.entity.*;
import com.windsome.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class CartServiceTest {

    @Autowired ItemRepository itemRepository;
    @Autowired AccountRepository accountRepository;
    @MockBean AccountService accountService;
    @Autowired CartService cartService;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired CartRepository cartRepository;
    @Autowired ItemImgRepository itemImgRepository;

    @Test
    @DisplayName("장바구니 추가 테스트")
    public void addCart() {
        Item item = saveItem();
        Account account = saveAccount();

        CartItemDto cartItemDto = CartItemDto.builder()
                .itemId(item.getId())
                .count(5)
                .build();

        Long cartItemId = cartService.addCart(cartItemDto, account.getUserIdentifier());

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        assertEquals(item.getId(), cartItem.getItem().getId());
        assertEquals(cartItemDto.getCount(), cartItem.getCount());
    }

    @Test
    @DisplayName("장바구니 수량 테스트")
    public void updateCartItemCount() {
        Item item = saveItem();
        Account account = saveAccount();

        CartItemDto cartItemDto = CartItemDto.builder()
                .itemId(item.getId())
                .count(5)
                .build();

        Long cartItemId = cartService.addCart(cartItemDto, account.getUserIdentifier());
        cartService.updateCartItemCount(cartItemId, 3);
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        assertNotEquals(cartItem.getCount(), cartItemDto.getCount());
        assertEquals(cartItem.getCount(), 3);
    }

    @Test
    @DisplayName("장바구니 취소 테스트")
    public void deleteCartItem() {
        Item item = saveItem();
        Account account = saveAccount();

        CartItemDto cartItemDto = CartItemDto.builder()
                .itemId(item.getId())
                .count(5)
                .build();

        Long cartItemId = cartService.addCart(cartItemDto, account.getUserIdentifier());
        cartService.deleteCartItem(cartItemId);

        assertThrows(EntityNotFoundException.class, () -> {
            cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        });
    }

    @Test
    @DisplayName("장바구니 아이템 조회 테스트")
    public void getCartList() {
        Account account = saveAccount();

        Item item1 = saveItem();
        saveItemImg(item1);

        Item item2 = saveItem();
        saveItemImg(item2);

        CartItemDto cartItemDto1 = CartItemDto.builder()
                .itemId(item1.getId())
                .count(3)
                .build();

        CartItemDto cartItemDto2 = CartItemDto.builder()
                .itemId(item2.getId())
                .count(6)
                .build();

        // Cart 생성 및 Cart Item 추가
        cartService.addCart(cartItemDto1, account.getUserIdentifier());
        cartService.addCart(cartItemDto2, account.getUserIdentifier());

        // Cart Item List 조회
        List<CartDetailDto> cartList = cartService.getCartList(account.getUserIdentifier());

        assertEquals(cartList.size(), 2);
        assertEquals(cartList.get(0).getItemId(), item2.getId());
    }

    public Item saveItem() {
        Item item = Item.builder()
                .itemNm("테스트 상품")
                .price(10000)
                .itemDetail("테스트 상품 상세 설명")
                .itemSellStatus(ItemSellStatus.SELL)
                .stockNumber(100)
                .build();
        return itemRepository.save(item);
    }

    public Account saveAccount() {
        Account account = Account.builder()
                .userIdentifier("gildong123")
                .password("gildong123")
                .name("gildong")
                .email("gildong@naver.com")
                .address1("test")
                .address2("test")
                .address3("test")
                .build();
        return accountRepository.save(account);
    }

    public void saveItemImg(Item item) {
        ItemImg itemImg = ItemImg.builder()
                .imgName("test imgName")
                .oriImgName("test oriImgName")
                .imgUrl("test imgUrl")
                .repImgYn("Y")
                .item(item)
                .build();
        itemImgRepository.save(itemImg);
    }
}