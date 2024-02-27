package com.windsome.service;

import com.windsome.dto.cart.CartDetailDto;
import com.windsome.dto.cart.CartProductDto;
import com.windsome.entity.*;
import com.windsome.repository.cart.CartRepository;
import com.windsome.repository.cartProduct.CartProductRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;

import static com.windsome.TestUtil.createMember;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class CartServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartProductRepository cartProductRepository;

    @InjectMocks private CartService cartService;

    @Test
    @DisplayName("장바구니 화면 조회 - 장바구니가 비어있는 경우")
    void testGetCartProducts_CartEmpty() {
        // Given
        String userIdentifier = "user123";
        Member member = new Member();
        member.setId(1L);
        Cart cart = null;

        when(memberRepository.findByUserIdentifier(userIdentifier)).thenReturn(member);
        when(cartRepository.findByMemberId(member.getId())).thenReturn(cart);

        // When
        List<CartDetailDto> cartProducts = cartService.getCartProducts(userIdentifier);

        // Then
        assertEquals(0, cartProducts.size());
        verify(memberRepository, times(1)).findByUserIdentifier(userIdentifier);
        verify(cartRepository, times(1)).findByMemberId(member.getId());
        verify(cartProductRepository, never()).findCartDetailDtoList(anyLong());
    }

    @Test
    @DisplayName("장바구니 화면 조회 - 장바구니에 상품이 있는 경우")
    void testGetCartProducts_CartNotEmpty() {
        // Given
        String userIdentifier = "user123";
        Member member = new Member();
        member.setId(1L);
        Cart cart = new Cart();
        cart.setId(1L);

        CartDetailDto cartDetailDto = new CartDetailDto(); // Mocked data
        List<CartDetailDto> expectedCartProducts = Collections.singletonList(cartDetailDto);

        when(memberRepository.findByUserIdentifier(userIdentifier)).thenReturn(member);
        when(cartRepository.findByMemberId(member.getId())).thenReturn(cart);
        when(cartProductRepository.findCartDetailDtoList(cart.getId())).thenReturn(expectedCartProducts);

        // When
        List<CartDetailDto> cartProducts = cartService.getCartProducts(userIdentifier);

        // Then
        assertEquals(1, cartProducts.size());
        assertEquals(expectedCartProducts, cartProducts);
        verify(memberRepository, times(1)).findByUserIdentifier(userIdentifier);
        verify(cartRepository, times(1)).findByMemberId(member.getId());
        verify(cartProductRepository, times(1)).findCartDetailDtoList(cart.getId());
    }

    @Test
    @DisplayName("상품이 장바구니에 성공적으로 추가됨")
    void testAddCartProduct_SuccessfullyAdded() {
        // Given
        CartProductDto cartProductDto = CartProductDto.builder()
                .productId(1L)
                .count(1)
                .build();
        Product product = Product.builder()
                .id(1L)
                .build();
        when(productRepository.findById(cartProductDto.getProductId())).thenReturn(java.util.Optional.of(product));

        Member member = Member.builder()
                .id(1L)
                .build();
        when(memberRepository.findByUserIdentifier("user1")).thenReturn(member);

        Cart cart = Cart.builder()
                .id(1L)
                .build();
        when(cartRepository.findByMemberId(member.getId())).thenReturn(cart);

        when(cartProductRepository.findByCartIdAndProductId(cart.getId(), product.getId())).thenReturn(null);

        // When
        assertDoesNotThrow(() -> cartService.addCartProduct(cartProductDto, "user1"));

        // Then
        verify(productRepository, times(1)).findById(cartProductDto.getProductId());
        verify(memberRepository, times(1)).findByUserIdentifier("user1");
        verify(cartRepository, times(1)).findByMemberId(member.getId());
        verify(cartProductRepository, times(1)).findByCartIdAndProductId(cart.getId(), product.getId());
        verify(cartProductRepository, times(1)).save(any(CartProduct.class));
    }

    @Test
    @DisplayName("상품이 존재하지 않을 때 예외 발생")
    void testAddCartProduct_ProductNotFound() {
        // Given
        CartProductDto cartProductDto = new CartProductDto();
        cartProductDto.setProductId(1L);
        when(productRepository.findById(cartProductDto.getProductId())).thenReturn(java.util.Optional.empty());

        // When, Then
        assertThrows(EntityNotFoundException.class, () -> cartService.addCartProduct(cartProductDto, "user1"));
    }

    @Test
    @DisplayName("회원이 존재하지 않을 때 예외 발생")
    void testAddCartProduct_MemberNotFound() {
        // Given
        CartProductDto cartProductDto = new CartProductDto();
        cartProductDto.setProductId(1L);
        Product product = new Product();
        when(productRepository.findById(cartProductDto.getProductId())).thenReturn(java.util.Optional.of(product));
        when(memberRepository.findByUserIdentifier("user1")).thenThrow(EntityNotFoundException.class);

        // When, Then
        assertThrows(EntityNotFoundException.class, () -> cartService.addCartProduct(cartProductDto, "user1"));
    }

    @Test
    @DisplayName("장바구니 수정/삭제 권한이 있는 경우")
    @Transactional(readOnly = true)
    public void testValidateCartModificationPermission_WithPermission() {
        // Given
        Long productId = 1L;
        String userIdentifier = "user1";

        CartProduct cartProduct = new CartProduct();
        Cart cart = new Cart();
        Member member = new Member();
        member.setUserIdentifier(userIdentifier);
        cart.setMember(member);
        cartProduct.setCart(cart);

        when(cartProductRepository.findById(productId)).thenReturn(java.util.Optional.of(cartProduct));

        // When
        boolean hasPermission = cartService.validateCartModificationPermission(productId, userIdentifier);

        // Then
        assertFalse(hasPermission);
        verify(cartProductRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(cartProductRepository);
    }

    @Test
    @DisplayName("장바구니 수정/삭제 권한이 없는 경우")
    @Transactional(readOnly = true)
    public void testValidateCartModificationPermission_WithoutPermission() {
        // Given
        Long productId = 1L;
        String userIdentifier = "user1";

        CartProduct cartProduct = new CartProduct();
        Cart cart = new Cart();
        Member member = new Member();
        member.setUserIdentifier("otherUser");
        cart.setMember(member);
        cartProduct.setCart(cart);

        when(cartProductRepository.findById(productId)).thenReturn(java.util.Optional.of(cartProduct));

        // When
        boolean hasPermission = cartService.validateCartModificationPermission(productId, userIdentifier);

        // Then
        assertTrue(hasPermission);
        verify(cartProductRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(cartProductRepository);
    }

    @Test
    @DisplayName("장바구니 상품이 존재하지 않을 때 예외 발생")
    @Transactional(readOnly = true)
    public void testValidateCartModificationPermission_CartProductNotFound() {
        // Given
        Long productId = 1L;
        String userIdentifier = "user1";

        when(cartProductRepository.findById(productId)).thenReturn(java.util.Optional.empty());

        // When, Then
        assertThrows(EntityNotFoundException.class, () -> cartService.validateCartModificationPermission(productId, userIdentifier));
        verify(cartProductRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(cartProductRepository);
    }

    @Test
    @DisplayName("장바구니 상품 개수 수정")
    public void testUpdateCartItemQuantity() {
        // Given
        Long productId = 1L;
        int newCount = 5;

        CartProduct cartProduct = new CartProduct();
        cartProduct.setCount(3);

        when(cartProductRepository.findById(productId)).thenReturn(java.util.Optional.of(cartProduct));

        // When
        cartService.updateCartItemQuantity(productId, newCount);

        // Then
        assertEquals(newCount, cartProduct.getCount());
        verify(cartProductRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(cartProductRepository);
    }

    @Test
    @DisplayName("장바구니 상품이 존재하지 않을 때 예외 발생")
    public void testUpdateCartItemQuantity_CartProductNotFound() {
        // Given
        Long productId = 1L;
        int newCount = 5;

        when(cartProductRepository.findById(productId)).thenReturn(java.util.Optional.empty());

        // When, Then
        assertThrows(EntityNotFoundException.class, () -> cartService.updateCartItemQuantity(productId, newCount));
        verify(cartProductRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(cartProductRepository);
    }

    @Test
    @DisplayName("장바구니 상품 삭제")
    public void testDeleteCartProduct() {
        // Given
        Long productId = 1L;

        // When
        cartService.deleteCartProduct(productId);

        // Then
        verify(cartProductRepository, times(1)).deleteById(productId);
        verifyNoMoreInteractions(cartProductRepository);
    }

    @Test
    @DisplayName("삭제할 장바구니 상품이 존재하지 않을 때 예외 발생")
    public void testDeleteCartProduct_CartProductNotFound() {
        // Given
        Long productId = 1L;

        doThrow(EntityNotFoundException.class).when(cartProductRepository).deleteById(productId);

        // When, Then
        assertThrows(EntityNotFoundException.class, () -> cartService.deleteCartProduct(productId));
        verify(cartProductRepository, times(1)).deleteById(productId);
        verifyNoMoreInteractions(cartProductRepository);
    }

    @Test
    @DisplayName("장바구니 상품 개수 조회 - 장바구니에 상품이 없는 경우")
    public void testGetTotalCartProductCount_NoProductsInCart() {
        // Given
        Member member = createMember(1L);
        when(cartRepository.findByMemberId(member.getId())).thenReturn(null);

        // When
        Long totalProductCount = cartService.getTotalCartProductCount(member);

        // Then
        assertEquals(0L, totalProductCount);
        verify(cartRepository, times(1)).findByMemberId(member.getId());
        verifyNoMoreInteractions(cartProductRepository);
    }

    @Test
    @DisplayName("장바구니 상품 개수 조회 - 장바구니에 상품이 있는 경우")
    public void testGetTotalCartProductCount_ProductsInCart() {
        // Given
        Member member = createMember(1L);
        Cart cart = Cart.createCart(member);

        Product product1 = new Product();
        Product product2 = new Product();

        CartProduct cartProduct1 = new CartProduct();
        cartProduct1.setCart(cart);
        cartProduct1.setProduct(product1);

        CartProduct cartProduct2 = new CartProduct();
        cartProduct2.setCart(cart);
        cartProduct2.setProduct(product2);

        when(cartRepository.findByMemberId(member.getId())).thenReturn(cart);
        when(cartProductRepository.countByCartId(cart.getId())).thenReturn(2L);

        // When
        Long totalProductCount = cartService.getTotalCartProductCount(member);

        // Then
        assertEquals(2L, totalProductCount);
        verify(cartRepository, times(1)).findByMemberId(member.getId());
        verify(cartProductRepository, times(1)).countByCartId(cart.getId());
        verifyNoMoreInteractions(cartProductRepository);
    }


}
