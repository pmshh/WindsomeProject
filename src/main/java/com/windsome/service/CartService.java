package com.windsome.service;

import com.windsome.dto.cart.CartDetailDto;
import com.windsome.dto.cart.CartProductDto;
import com.windsome.entity.Cart;
import com.windsome.entity.CartProduct;
import com.windsome.entity.Member;
import com.windsome.entity.Product;
import com.windsome.repository.cartProduct.CartProductRepository;
import com.windsome.repository.cart.CartRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;

    /**
     * 장바구니 화면 조회
     */
    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartProducts(String userIdentifier) {
        Member member = memberRepository.findByUserIdentifier(userIdentifier);
        Cart cart = cartRepository.findByMemberId(member.getId());

        return cart != null ? cartProductRepository.findCartDetailDtoList(cart.getId()) : Collections.emptyList();
    }

    /**
     * 장바구니 상품 추가
     */
    public void addCartProduct(CartProductDto cartProductDto, String userIdentifier) {
        Product product = productRepository.findById(cartProductDto.getProductId()).orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByUserIdentifier(userIdentifier);

        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        CartProduct savedCartProduct = cartProductRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (savedCartProduct != null) {
            savedCartProduct.addCount(cartProductDto.getCount());
        } else {
            CartProduct cartProduct = CartProduct.createCartProduct(cart, product, cartProductDto.getCount());
            cartProductRepository.save(cartProduct);
        }
    }

    /**
     * 장바구니 수정 권한 검증
     */
    @Transactional(readOnly = true)
    public boolean validateCartModificationPermission(Long productId, String userIdentifier) {
        CartProduct cartProduct = cartProductRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        Member savedAccount = cartProduct.getCart().getMember();
        return !(savedAccount.getUserIdentifier().equals(userIdentifier));
    }

    /**
     * 장바구니 삭제 권한 검증
     */
    public boolean validateCartDeletionPermission(Long[] productIds, String userIdentifier) {
        for (Long productId : productIds) {
            CartProduct cartProduct = cartProductRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
            Member savedAccount = cartProduct.getCart().getMember();
            if (!savedAccount.getUserIdentifier().equals(userIdentifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 장바구니 상품 개수 수정
     */
    public void updateCartItemQuantity(Long productId, int count) {
        CartProduct cartProduct = cartProductRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        cartProduct.setCount(count);
    }

    /**
     * 장바구니 상품 삭제
     */
    public void deleteCartProduct(Long[] productIds) {
        for (Long productId : productIds) {
            cartProductRepository.deleteById(productId);
        }
    }

    /**
     * 장바구니 상품 개수 조회
     */
    @Transactional(readOnly = true)
    public Long getTotalCartProductCount(Member member) {
        Cart cart = cartRepository.findByMemberId(member.getId());
        return cart != null ? cartProductRepository.countByCartId(cart.getId()) : 0L;
    }
}
