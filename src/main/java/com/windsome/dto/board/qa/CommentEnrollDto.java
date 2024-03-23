package com.windsome.dto.board.qa;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentEnrollDTO {

    private Long qaId;

    private String content;

    private boolean hasPrivate;
}
