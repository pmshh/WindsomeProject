package com.windsome.dto.board.qa;

import com.windsome.constant.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long commentId; // 기본키

    private String userIdentifier; // 작성자 id

    private String createdBy; // 작성자 이름

    private String role; // 작성자 권한 (관리자 일시 작성자 이름 전체 공개, 일반 유저 일시 작성자 이름 일부 공개)

    private LocalDateTime regTime; // 댓글 등록 시간

    private String content; // 댓글 내용

    private boolean secretYN; // 비밀글 설정 여부

    public CommentDto(String content) {
        this.content = content;
    }

    public CommentDto(Long commentId, String userIdentifier, String createdBy, Role state, LocalDateTime regTime, String content, boolean secretYN) {
        this.commentId = commentId;
        this.userIdentifier = userIdentifier;
        this.createdBy = createdBy;
        String role = null;
        if (state == Role.ADMIN) {
            role = "admin";
        } else {
            role = "user";
        }
        this.role = role;
        this.regTime = regTime;
        this.content = content;
        this.secretYN = secretYN;
    }
}
