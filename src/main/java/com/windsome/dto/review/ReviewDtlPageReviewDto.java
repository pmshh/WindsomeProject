package com.windsome.dto.review;

import com.windsome.entity.ItemImg;
import com.windsome.entity.Review;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ReviewDtlPageReviewDto {

    /**
     * 리뷰 정보
     */
    private Long reviewId;

    private String title;

    private BigDecimal rating;

    private String content;

    private String password;

    /**
     * 상품 정보
     */
    private String imgUrl;

    private Long itemId;

    private String itemNm;

    private int price;

    private double discount;

    private int salePrice;

    public static ReviewDtlPageReviewDto createReviewDtlPageDto(Review review, ItemImg itemImg) {
        ReviewDtlPageReviewDto reviewDtlPageReviewDto = new ReviewDtlPageReviewDto();
        reviewDtlPageReviewDto.setReviewId(review.getId());
        reviewDtlPageReviewDto.setTitle(review.getTitle());
        reviewDtlPageReviewDto.setRating(review.getRating());
        reviewDtlPageReviewDto.setContent(review.getContent());
        reviewDtlPageReviewDto.setPassword(review.getPassword());

        reviewDtlPageReviewDto.setImgUrl(itemImg.getImgUrl());
        reviewDtlPageReviewDto.setItemId(review.getItem().getId());
        reviewDtlPageReviewDto.setItemNm(review.getItem().getItemNm());
        reviewDtlPageReviewDto.setPrice(review.getItem().getPrice());
        reviewDtlPageReviewDto.setDiscount(review.getItem().getDiscount());
        reviewDtlPageReviewDto.setSalePrice((int) Math.floor(review.getItem().getPrice() * (1 - review.getItem().getDiscount())));
        return reviewDtlPageReviewDto;
    }
}
