package com.windsome.dto.board.review;

import com.windsome.entity.board.Board;
import com.windsome.entity.product.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Data
public class ProductReviewDTO {

    private Long reviewId; // 리뷰 id

    private String content; // 내용

    private String regDate; // 작성일

    private String createdBy; // 작성자

    private BigDecimal rating; // 평점

    private int hits; // 조회수

    public static ProductReviewDTO createProductReviewDTO(Board review, Product product) {
        ProductReviewDTO productReviewDTO = new ProductReviewDTO();
        productReviewDTO.setReviewId(review.getId());
        productReviewDTO.setContent(review.getContent());
        productReviewDTO.setRegDate(review.getRegTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        productReviewDTO.setCreatedBy(review.getMember().getName());
        productReviewDTO.setRating(review.getRating());
        productReviewDTO.setHits(review.getHits());
        return productReviewDTO;
    }
}
