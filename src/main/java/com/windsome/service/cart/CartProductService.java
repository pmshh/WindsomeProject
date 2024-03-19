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
    public CartProduct getCartProductByProductIdAndColorIdAndSizeId(Long productId, Long colorId, Long sizeId) {
        return cartProductRepository.findByProductIdAndColorIdAndSizeId(productId, colorId, sizeId);
    }

    /**
     * 장바구니 상품 삭제
     */
    public void deleteCartProduct(CartProduct cartProduct) {
        cartProductRepository.delete(cartProduct);
    }
}
