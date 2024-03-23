package com.windsome.dto.board.notice;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor
public class NoticeListDTO {

    private Long noticeId;

    private String title;

    private String content;

    private String createdBy;

    private String regDate;

    private boolean hasNotice;

    @QueryProjection
    public NoticeListDTO(Long noticeId, String title, String content, String createdBy, LocalDateTime regDate, boolean hasNotice) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.regDate = regDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.hasNotice = hasNotice;
    }
}
