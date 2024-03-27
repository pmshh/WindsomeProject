package com.windsome.service.cart;

import com.windsome.entity.cart.CartProduct;
import com.windsome.repository.cartProduct.CartProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartProductService {

    private final CartProductRepository cartProductRepository;

    /**
     * 장바구니 상품 조회
     */
    public CartProduct getCartProductByProductIdAndColorAndSize(Long productId, String color, String size) {
        return cartProductRepository.findByProductIdAndColorAndSize(productId, color, size);
    }

    /**
     * 장바구니 상품 삭제
     */
    public void deleteCartProduct(CartProduct cartProduct) {
        cartProductRepository.delete(cartProduct);
    }
}
