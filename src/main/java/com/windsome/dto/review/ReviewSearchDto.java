package com.windsome.dto.review;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewSearchDto {

    private String searchDateType; // 제목, 내용, 작성자

    private String searchQuery = "";
}
