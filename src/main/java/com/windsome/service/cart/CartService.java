package com.windsome.service.cart;

import com.windsome.dto.cart.CartDetailDto;
import com.windsome.dto.cart.CartProductDTO;
import com.windsome.dto.cart.CartProductListDTO;
import com.windsome.entity.*;
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
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;

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
    public void addCartProduct(CartProductListDTO cartProductListDTO, String userIdentifier) {
        // 상품, 회원 조회
        Product product = productRepository.findById(cartProductListDTO.getProductId()).orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByUserIdentifier(userIdentifier);

        // 기존 장바구니가 존재하지 않는다면, 새로운 장바구니를 만들고 DB에 저장
        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        // 장바구니에 상품이 이미 담겨있다면, 상품의 수량을 업데이트하고, 담겨있지 않다면 새로운 상품을 장바구니에 추가
        for (CartProductDTO cartProductDTO : cartProductListDTO.getCartProductDTOList()) {
            CartProduct findCartProduct = cartProductRepository.findByProductIdAndColorIdAndSizeId(product.getId(), cartProductDTO.getColorId(), cartProductDTO.getSizeId());

            if (findCartProduct != null) {
                findCartProduct.addQuantity(cartProductDTO.getQuantity());
            } else {
                Color color = colorRepository.findById(cartProductDTO.getColorId()).orElseThrow(EntityNotFoundException::new);
                Size size = sizeRepository.findById(cartProductDTO.getSizeId()).orElseThrow(EntityNotFoundException::new);
                CartProduct cartProduct = CartProduct.createCartProduct(cart, product, color, size, cartProductDTO.getQuantity());
                cartProductRepository.save(cartProduct);
            }
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
    public void updateCartItemQuantity(Long productId, int quantity) {
        CartProduct cartProduct = cartProductRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        cartProduct.setQuantity(quantity);
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
