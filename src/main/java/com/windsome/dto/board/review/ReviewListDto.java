package com.windsome.dto.board.review;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor
public class ReviewListDto {

    private Long reviewId;

    private String title;

    private String content;

    private BigDecimal rating;

    private String createdBy;

    private String regDate;

    private int hits;

    /**
     * 상품 정보
     */
    private String imageUrl;

    private Long productId;

    private String productName;

    private int price;

    private double discount;

    private int salePrice;

    @QueryProjection
    public ReviewListDto(Long reviewId, String title, String content, BigDecimal rating, String createdBy, LocalDateTime regDate, int hits, String imageUrl, Long productId, String productName, int price, double discount) {
        this.reviewId = reviewId;
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.createdBy = createdBy;
        this.regDate = regDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.hits = hits;
        this.imageUrl = imageUrl;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.discount = discount;
        this.salePrice = (int) Math.ceil(price * (1 - discount));
    }
}
