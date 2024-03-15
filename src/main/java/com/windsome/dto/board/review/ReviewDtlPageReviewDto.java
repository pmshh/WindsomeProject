package com.windsome.dto.board.review;

import com.windsome.entity.product.ProductImage;
import com.windsome.entity.board.Review;
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
    private String imageUrl;

    private Long productId;

    private String productName;

    private int price;

    private double discount;

    private int salePrice;

    public static ReviewDtlPageReviewDto createReviewDtlPageDto(Review review, ProductImage productImage) {
        ReviewDtlPageReviewDto reviewDtlPageReviewDto = new ReviewDtlPageReviewDto();
        reviewDtlPageReviewDto.setReviewId(review.getId());
        reviewDtlPageReviewDto.setTitle(review.getTitle());
        reviewDtlPageReviewDto.setRating(review.getRating());
        reviewDtlPageReviewDto.setContent(review.getContent());
        reviewDtlPageReviewDto.setPassword(review.getPassword());

        reviewDtlPageReviewDto.setImageUrl(productImage.getImageUrl());
        reviewDtlPageReviewDto.setProductId(review.getProduct().getId());
        reviewDtlPageReviewDto.setProductName(review.getProduct().getName());
        reviewDtlPageReviewDto.setPrice(review.getProduct().getPrice());
        reviewDtlPageReviewDto.setDiscount(review.getProduct().getDiscount());
        reviewDtlPageReviewDto.setSalePrice((int) Math.floor(review.getProduct().getPrice() * (1 - review.getProduct().getDiscount())));
        return reviewDtlPageReviewDto;
    }
}
