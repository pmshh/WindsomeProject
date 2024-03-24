package com.windsome.controller.admin;

import com.windsome.dto.board.SearchDTO;
import com.windsome.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminBoardController {

    private final BoardService boardService;

    /**
     * 게시판 관리(Notice) - 조회
     */
    @GetMapping("/board/notices")
    public String getNoticeList(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("noticeList", boardService.getNoticeList(searchDTO, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("fixTopNoticeList", boardService.getFixTopNoticeList());
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "admin/board/notice-board-management";
    }

    /**
     * 게시판 관리(Notice) - 게시글 삭제
     */
    @DeleteMapping("/board/notices")
    public ResponseEntity<String> deleteNotice(@RequestParam(value = "noticeIds") Long[] noticeIds) {
        try {
            boardService.deletePosts(noticeIds);
            return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }

    /**
     * 게시판 관리(Notice) - 게시글 수정
     */
    @PatchMapping("/board/notices/{noticeId}")
    public ResponseEntity<String> updateNotice(@PathVariable Long noticeId, boolean noticeYn) {
        try {
            if (boardService.checkNoticeYN(noticeId, noticeYn)) {
                return ResponseEntity.badRequest().body("이미 공지글로 설정되어있습니다.");
            }
            boardService.updateNoticeYN(noticeId, noticeYn);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }

    /**
     * 게시판 관리(Q&A) - 조회
     */
    @GetMapping("/board/qa")
    public String getQaList(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        model.addAttribute("qaList", boardService.getQaList(searchDTO, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "admin/board/qa-board-management";
    }

    /**
     * 게시판 관리(Q&A) - 게시글 삭제
     */
    @DeleteMapping("/board/qa")
    public ResponseEntity<String> deleteQa(Long[] qaIds) {
        try {
            boardService.deletePosts(qaIds);
            return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }

    /**
     * 게시판 관리(Review) - 조회
     */
    @GetMapping("/board/reviews")
    public String getReviewList(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        model.addAttribute("reviews", boardService.getReviews(searchDTO, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("maxPage", 10);
        return "admin/board/review-board-management";
    }

    /**
     * 게시판 관리(Review) - 게시글 삭제
     */
    @DeleteMapping("/board/reviews")
    public ResponseEntity<String> deleteReview(Long[] reviewIds) {
        try {
            boardService.deletePosts(reviewIds);
            return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }
}
