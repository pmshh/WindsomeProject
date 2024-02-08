package com.windsome.dto.board.notice;

import java.time.LocalDateTime;

public interface NoticeDtlDtoInterface {

    Long getNoticeId();
    String getTitle();
    String getContent();
    boolean getNoticeYN();
    Long getAccountId();
    LocalDateTime getRegTime();
}
