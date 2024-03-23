package com.windsome.dto.board.qa;

import com.querydsl.core.annotations.QueryProjection;
import com.windsome.constant.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor
public class QaListDTO {

    private Long qaId; // qa 기본키

    private String title; // 제목

    private String content;  // 내용

    private String createdBy; // 작성자

    private Role state; // 사용자 권한

    private String regDate; // 작성일

    private boolean hasPrivate; // 비밀 글 설정 여부

    private Long originNo; // 원글 번호

    private int groupOrder; // 원글(답글 포함)에 대한 순서

    private int groupLayer; // 답글 계층

    @QueryProjection
    public QaListDTO(Long qaId, String title, String content, String createdBy, Role state, LocalDateTime regDate, boolean hasPrivate, Long originNo, int groupOrder, int groupLayer) {
        this.qaId = qaId;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.state = state;
        this.regDate = regDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.hasPrivate = hasPrivate;
        this.originNo = originNo;
        this.groupOrder = groupOrder;
        this.groupLayer = groupLayer;
    }

}
