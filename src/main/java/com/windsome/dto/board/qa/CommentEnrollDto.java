package com.windsome.dto.board.qa;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentEnrollDto {

    private Long qaId;

    private String content;

    private boolean secretYN;
}
