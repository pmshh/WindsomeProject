package com.windsome.dto.board.qa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class QaSearchDto {

    private String searchDateType; // 제목, 내용, 작성자

    private String searchQuery = "";
}
