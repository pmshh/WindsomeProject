package com.windsome.dto.board.review;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter @Setter
public class ReviewListDto {

    private Long reviewId;

    private String title;

    private BigDecimal rating;

    private String createdBy;

    private String regDate;

    private int hits;

    /**
     * 상품 정보
     */
    private String imgUrl;

    private Long itemId;

    private String itemNm;

    private int price;

    private double discount;

    private int salePrice;

    @QueryProjection
    public ReviewListDto(Long reviewId, String title, BigDecimal rating, String createdBy, LocalDateTime regDate, int hits, String imgUrl, Long itemId, String itemNm, int price, double discount) {
        this.reviewId = reviewId;
        this.title = title;
        this.rating = rating;
        this.createdBy = createdBy;
        this.regDate = regDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.hits = hits;
        this.imgUrl = imgUrl;
        this.itemId = itemId;
        this.itemNm = itemNm;
        this.price = price;
        this.discount = discount;
        this.salePrice = (int) Math.ceil(price * (1 - discount));
    }
}
