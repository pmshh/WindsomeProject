package com.windsome.repository;

import com.windsome.dto.review.ItemListDto;
import com.windsome.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item>, ItemRepositoryCustom {

    @Query(value = "select new com.windsome.dto.review.ItemListDto(im.imgUrl, i.id, i.itemNm, i.price, i.discount) from Item i join ItemImg im on i.id = im.item.id where im.repImgYn = 'Y' and i.itemNm like %:itemNm% order by i.id desc")
    List<ItemListDto> getReviewPageItemList(@Param("itemNm") String itemNm, Pageable pageable);

    @Query(value = "select count(i) from Item i join ItemImg im on i.id = im.item.id where im.repImgYn = 'Y' and i.itemNm like %:itemNm%")
    Long getReviewPageItemListCount(@Param("itemNm") String itemNm);
}
