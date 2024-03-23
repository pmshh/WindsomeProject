package com.windsome.dto.board.qa;

import com.windsome.entity.board.Board;
import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QaUpdateDTO {

    private Long qaId;

    private String title;

    private String content;

    private String password;

    private boolean hasPrivate;

    public static QaUpdateDTO createDto(Board qa) {
        return QaUpdateDTO.builder()
                .qaId(qa.getId())
                .title(qa.getTitle())
                .content(qa.getContent())
                .password(qa.getPassword())
                .hasPrivate(qa.isHasPrivate())
                .build();
    }
}
