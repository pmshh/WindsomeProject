package com.windsome.dto.board.qa;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaDtlDTO {

    private Long qaId;

    private String title;

    private String content;

    private String password;

    private boolean hasPrivate;

    private LocalDateTime regTime;

    private String createdBy;

    public QaDtlDTO(QaDtlDtoInterface qaDtlDtoInterface, String createdBy) {
        this.qaId = qaDtlDtoInterface.getBoardId();
        this.title = qaDtlDtoInterface.getTitle();
        this.content = qaDtlDtoInterface.getContent();
        this.password = qaDtlDtoInterface.getPassword();
        this.hasPrivate = qaDtlDtoInterface.getHasPrivate();
        this.regTime = qaDtlDtoInterface.getRegTime();
        this.createdBy = createdBy;
    }
}
