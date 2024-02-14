package com.windsome.controller.board;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.board.qa.CommentEnrollDto;
import com.windsome.dto.board.qa.CommentUpdateDto;
import com.windsome.entity.Account;
import com.windsome.service.board.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 등록
     */
    @PostMapping("/comment/enroll")
    public ResponseEntity<String> enrollComment(@CurrentAccount Account account, CommentEnrollDto commentEnrollDto) {
        if (account == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }
        try {
            commentService.enrollComment(commentEnrollDto, account);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
        return ResponseEntity.ok().body("댓글이 등록되었습니다.");
    }

    /**
     * 댓글 수정
     */
    @PatchMapping("/comment/{commentId}")
    public ResponseEntity<String> updateComment(@CurrentAccount Account account, CommentUpdateDto commentUpdateDto) {
        if (account == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }

        try {
            if (commentService.validateComment(account, commentUpdateDto.getCommentId())) {
                return ResponseEntity.badRequest().body("댓글 수정 권한이 없습니다.");
            }
            commentService.updateComment(commentUpdateDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 댓글입니다.");
        }
        return ResponseEntity.ok().body("댓글이 수정되었습니다.");
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@CurrentAccount Account account, @PathVariable Long commentId) {
        if (account == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }

        try {
            if (commentService.validateComment(account, commentId)) {
                return ResponseEntity.badRequest().body("댓글 수정 권한이 없습니다.");
            }
            commentService.deleteComment(commentId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 댓글입니다.");
        }
        return ResponseEntity.ok().body("댓글이 삭제되었습니다.");
    }
}
