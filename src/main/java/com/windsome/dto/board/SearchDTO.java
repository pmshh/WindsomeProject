package com.windsome.dto.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchDTO {

    private String searchDateType; // 제목, 내용, 작성자

    private String searchQuery = "";

}
