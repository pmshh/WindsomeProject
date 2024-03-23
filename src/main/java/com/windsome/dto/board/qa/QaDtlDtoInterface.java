package com.windsome.dto.board.qa;

import java.time.LocalDateTime;

public interface QaDtlDtoInterface {

    Long getBoardId();
    String getTitle();
    String getContent();
    String getPassword();
    boolean getHasPrivate();
    LocalDateTime getRegTime();
    Long getMemberId();
}
