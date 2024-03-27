package com.windsome.repository.cartProduct;

import com.windsome.dto.cart.CartDetailDTO;
import com.windsome.entity.cart.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {

    CartProduct findByProductIdAndColorAndSize(Long productId, String color, String size);

    @Query("select new com.windsome.dto.cart.CartDetailDTO(cp.id, p.id, p.name, p.discount, cp.color, cp.size, coalesce(po.quantity, p.inventory), cp.quantity, p.price, pi.imageUrl) " +
            "from CartProduct cp " +
            "join ProductImage pi " +
            "on cp.product.id = pi.product.id " +
            "join Product p " +
            "on cp.product.id = p.id " +
            "left join ProductOption po " +
            "on cp.product.id = po.product.id " +
            "and po.color = cp.color " +
            "and po.size = cp.size " +
            "where pi.isRepresentativeImage = true " +
            "order by cp.regTime desc")
    List<CartDetailDTO> findCartDetailDtoList(@Param("cartId") Long cartId);

    Long countByCartId(Long cartId);
}
