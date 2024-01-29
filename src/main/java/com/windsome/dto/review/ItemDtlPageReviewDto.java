package com.windsome.dto.review;

import com.windsome.entity.Item;
import com.windsome.entity.Review;
import lombok.Data;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Data
public class ItemDtlPageReviewDto {

    private Long reviewId; // 리뷰 id

    private String title; // 제목

    private String regDate; // 작성일

    private String createdBy; // 작성자

    private BigDecimal rating; // 평점

    private int hits; // 조회수

    public static ItemDtlPageReviewDto createItemDtlPageReviewDto(Review review, Item item) {
        ItemDtlPageReviewDto itemDtlPageReviewDto = new ItemDtlPageReviewDto();
        itemDtlPageReviewDto.setReviewId(review.getId());
        itemDtlPageReviewDto.setTitle(review.getTitle());
        itemDtlPageReviewDto.setRegDate(review.getRegDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        itemDtlPageReviewDto.setCreatedBy(review.getAccount().getName());
        itemDtlPageReviewDto.setRating(review.getRating());
        itemDtlPageReviewDto.setHits(review.getHits());
        return itemDtlPageReviewDto;
    }
}
