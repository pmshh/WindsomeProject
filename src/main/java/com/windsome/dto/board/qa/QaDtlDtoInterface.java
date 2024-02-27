package com.windsome.dto.board.qa;

import java.time.LocalDateTime;

public interface QaDtlDtoInterface {

    Long getQaId();
    String getTitle();
    String getContent();
    String getPassword();
    boolean getSecretYN();
    LocalDateTime getRegTime();
    Long getMemberId();
}
