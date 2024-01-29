package com.windsome.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEnrollDto {

    private Long itemId; // 상품 id

    private String title; // 제목

    private BigDecimal rating; // 평점

    private String content; // 내용

    private String password; // 비밀번호

}
