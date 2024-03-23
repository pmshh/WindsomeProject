package com.windsome.dto.board.notice;

import java.time.LocalDateTime;

public interface NoticeDtlDtoInterface {

    Long getBoardId();
    String getTitle();
    String getContent();
    boolean getHasNotice();
    Long getMemberId();
    LocalDateTime getRegTime();
}
