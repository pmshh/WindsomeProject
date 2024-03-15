package com.windsome.repository.cartProduct;

import com.windsome.dto.cart.CartDetailDto;
import com.windsome.entity.cart.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

    CartProduct findByProductIdAndColorIdAndSizeId(Long productId, Long colorId, Long sizeId);

    CartProduct findByProductId(Long productId);

    @Query("select new com.windsome.dto.cart.CartDetailDto(cp.id, p.id, p.name, p.discount, cp.color.id, c.name, cp.size.id, s.name, i.quantity, cp.quantity, p.price, pi.imageUrl) " +
            "from CartProduct cp, ProductImage pi " +
            "join cp.product p " +
            "join Inventory i on i.color.id = cp.color.id and i.size.id = cp.size.id and i.product.id = cp.product.id " +
            "join Color c on c.id = cp.color.id " +
            "join Size s on s.id = cp.size.id " +
            "where cp.cart.id = :cartId " +
            "and pi.product.id = cp.product.id " +
            "and pi.isRepresentativeImage = true " +
            "order by cp.regTime desc")
    List<CartDetailDto> findCartDetailDtoList(@Param("cartId") Long cartId);

    Long countByCartId(Long cartId);

}
