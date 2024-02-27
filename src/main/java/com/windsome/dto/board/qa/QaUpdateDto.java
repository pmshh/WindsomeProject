package com.windsome.dto.board.qa;

import com.windsome.entity.board.Qa;
import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QaUpdateDto {

    private Long qaId;

    private String title;

    private String content;

    private String password;

    private boolean secretYN;

    public static QaUpdateDto createDto(Qa qa) {
        return QaUpdateDto.builder()
                .qaId(qa.getId())
                .title(qa.getTitle())
                .content(qa.getContent())
                .password(qa.getPassword())
                .secretYN(qa.isSecretYN())
                .build();
    }
}
