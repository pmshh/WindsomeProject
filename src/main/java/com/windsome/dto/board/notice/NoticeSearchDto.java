package com.windsome.dto.board.notice;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class NoticeSearchDto {

    private String searchDateType; // 제목, 내용, 작성자

    private String searchQuery = "";
}
