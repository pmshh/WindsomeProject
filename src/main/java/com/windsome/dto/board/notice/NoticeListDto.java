package com.windsome.dto.board.notice;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor
public class NoticeListDto {

    private Long noticeId;

    private String title;

    private String content;

    private String createdBy;

    private String regDate;

    private boolean noticeYN;

    @QueryProjection
    public NoticeListDto(Long noticeId, String title, String content, String createdBy, LocalDateTime regDate, boolean noticeYN) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.regDate = regDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.noticeYN = noticeYN;
    }
}
