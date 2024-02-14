package com.windsome.dto.board.qa;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaDtlDto {

    private Long qaId;

    private String title;

    private String content;

    private String password;

    private boolean secretYN;

    private LocalDateTime regTime;

    private String createdBy;

    public QaDtlDto(QaDtlDtoInterface qaDtlDtoInterface, String createdBy) {
        this.qaId = qaDtlDtoInterface.getQaId();
        this.title = qaDtlDtoInterface.getTitle();
        this.content = qaDtlDtoInterface.getContent();
        this.password = qaDtlDtoInterface.getPassword();
        this.secretYN = qaDtlDtoInterface.getSecretYN();
        this.regTime = qaDtlDtoInterface.getRegTime();
        this.createdBy = createdBy;
    }
}
