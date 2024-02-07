package com.windsome.repository;

import com.windsome.dto.cart.CartDetailDto;
import com.windsome.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    CartItem findByItemId(Long itemId);

    @Query("select new com.windsome.dto.cart.CartDetailDto(ci.id, i.id, i.itemNm, i.stockNumber, i.discount, ci.count, i.price, im.imgUrl) " +
            "from CartItem ci, ItemImg im " +
            "join ci.item i " +
            "where ci.cart.id = :cartId " +
            "and im.item.id = ci.item.id " +
            "and im.repImgYn = 'Y' " +
            "order by ci.regTime desc")
    List<CartDetailDto> findCartDetailDtoList(@Param("cartId") Long cartId);

    Long countByCartId(Long cartId);

}
