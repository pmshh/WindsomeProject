package com.windsome.dto.review;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ReviewUpdateDto {

    private Long reviewId;

    private String title;

    private BigDecimal rating;

    private String content;

    private String password;
}
