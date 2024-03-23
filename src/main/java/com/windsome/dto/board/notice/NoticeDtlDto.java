package com.windsome.dto.board.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDtlDTO {

    public Long noticeId;

    public String title;

    public String content;

    public boolean hasNotice;

    public String createdBy;

    public LocalDateTime regTime;

    public NoticeDtlDTO(NoticeDtlDtoInterface noticeDtlDtoInterface, String createdBy) {
        this.noticeId = noticeDtlDtoInterface.getBoardId();
        this.title = noticeDtlDtoInterface.getTitle();
        this.content = noticeDtlDtoInterface.getContent();
        this.hasNotice = noticeDtlDtoInterface.getHasNotice();
        this.createdBy = createdBy;
        this.regTime = noticeDtlDtoInterface.getRegTime();
    }
}
