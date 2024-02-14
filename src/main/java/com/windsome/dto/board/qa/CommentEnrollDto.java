package com.windsome.dto.board.qa;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentEnrollDto {

    private Long qaId;
    private String content;
    private boolean secretYN;
}
