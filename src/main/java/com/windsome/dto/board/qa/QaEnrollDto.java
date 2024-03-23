package com.windsome.dto.board.qa;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QaEnrollDTO {

    private String title;

    private String content;

    private String password;

    private boolean hasPrivate; // 비밀 글 설정 여부

    private Long originNo; // 원글 번호

    private int groupOrder; // 원글(답글 포함)에 대한 순서

    private int groupLayer; // 답글 계층
}
