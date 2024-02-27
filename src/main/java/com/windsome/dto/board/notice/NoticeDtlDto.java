package com.windsome.dto.board.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDtlDto {

    public Long noticeId;

    public String title;

    public String content;

    public boolean noticeYN;

    public String createdBy;

    public LocalDateTime regTime;

    public NoticeDtlDto(NoticeDtlDtoInterface noticeDtlDtoInterface, String createdBy) {
        this.noticeId = noticeDtlDtoInterface.getNoticeId();
        this.title = noticeDtlDtoInterface.getTitle();
        this.content = noticeDtlDtoInterface.getContent();
        this.noticeYN = noticeDtlDtoInterface.getNoticeYN();
        this.createdBy = createdBy;
        this.regTime = noticeDtlDtoInterface.getRegTime();
    }
}
