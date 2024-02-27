package com.windsome.dto.board.qa;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateDto {

    private Long commentId; // 댓글 번호

    private String content; // 댓글 내용

    private boolean secretYN; // 비밀글 설정 여부
}
