package com.windsome.repository.board.review;

import com.windsome.entity.board.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, QuerydslPredicateExecutor<Review>, ReviewRepositoryCustom {

    List<Review> findByProductIdOrderByIdDesc(Long productId, Pageable pageable);

    Long countByProductId(Long productId);

    Page<Review> findAll(Pageable pageable);

    @Query(value = "select avg(r.rating) from Review r where r.product.id = :productId")
    BigDecimal getRatingAvg(@Param("productId") Long productId);

    boolean existsByProductIdAndMemberId(Long productId, Long memberId);
}
