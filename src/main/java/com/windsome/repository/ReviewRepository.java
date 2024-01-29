package com.windsome.repository;

import com.windsome.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, QuerydslPredicateExecutor<Review>, ReviewRepositoryCustom {

    List<Review> findByItemIdOrderByIdDesc(Long itemId, Pageable pageable);

    Long countByItemId(Long itemId);

    Page<Review> findAll(Pageable pageable);

    Review findByItemId(Long itemId);

    @Query(value = "select avg(r.rating) from Review r where r.item.id = :itemId")
    BigDecimal getRatingAvg(@Param("itemId") Long itemId);
}
