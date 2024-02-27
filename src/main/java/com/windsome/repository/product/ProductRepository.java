package com.windsome.repository.product;

import com.windsome.dto.board.review.ProductListDto;
import com.windsome.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, QuerydslPredicateExecutor<Product>, ProductRepositoryCustom {

    @Query(value = "select new com.windsome.dto.board.review.ProductListDto(pm.imageUrl, p.id, p.name, p.price, p.discount) from Product p join ProductImage pm on p.id = pm.product.id where pm.isRepresentativeImage = true and p.name like %:name% order by p.id desc")
    List<ProductListDto> getReviewPageItemList(@Param("name") String name, Pageable pageable);

    @Query(value = "select count(p) from Product p join ProductImage pm on p.id = pm.product.id where pm.isRepresentativeImage = true and p.name like %:name%")
    Long getReviewPageItemListCount(@Param("name") String name);
}
