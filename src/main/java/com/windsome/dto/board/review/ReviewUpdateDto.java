package com.windsome.dto.board.review;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewUpdateDto {

    private Long reviewId;

    private Long productId;

    private String title;

    private BigDecimal rating;

    private String content;

    private String password;
}
