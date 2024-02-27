package com.windsome.repository.cartProduct;

import com.windsome.dto.cart.CartDetailDto;
import com.windsome.entity.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

    CartProduct findByCartIdAndProductId(Long cartId, Long productId);

    CartProduct findByProductId(Long productId);

    @Query("select new com.windsome.dto.cart.CartDetailDto(cp.id, p.id, p.name, p.stockNumber, p.discount, cp.count, p.price, pi.imageUrl) " +
            "from CartProduct cp, ProductImage pi " +
            "join cp.product p " +
            "where cp.cart.id = :cartId " +
            "and pi.product.id = cp.product.id " +
            "and pi.isRepresentativeImage = true " +
            "order by cp.regTime desc")
    List<CartDetailDto> findCartDetailDtoList(@Param("cartId") Long cartId);

    Long countByCartId(Long cartId);

}
