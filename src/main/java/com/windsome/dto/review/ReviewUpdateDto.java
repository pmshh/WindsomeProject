package com.windsome.dto.review;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewUpdateDto {

    private Long reviewId;

    private Long itemId;

    private String title;

    private BigDecimal rating;

    private String content;

    private String password;
}
