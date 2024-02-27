package com.windsome.controller.admin;

import com.windsome.dto.board.notice.NoticeSearchDto;
import com.windsome.dto.board.qa.QaSearchDto;
import com.windsome.dto.board.review.ReviewSearchDto;
import com.windsome.service.board.NoticeService;
import com.windsome.service.board.QaService;
import com.windsome.service.board.ReviewService;
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

    private final NoticeService noticeService;
    private final QaService qaService;
    private final ReviewService reviewService;

    /**
     * 게시판 관리(Notice) - 조회
     */
    @GetMapping("/board/notice")
    public String getNoticeList(NoticeSearchDto noticeSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("noticeSearchDto", noticeSearchDto);
        model.addAttribute("noticeList", noticeService.getNoticeList(noticeSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("fixTopNoticeList", noticeService.getFixTopNoticeList());
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "admin/board/notice-board-management";
    }

    /**
     * 게시판 관리(Notice) - 게시글 삭제
     */
    @DeleteMapping("/board/notice")
    public ResponseEntity<String> deleteNotice(@RequestParam(value = "noticeIds") Long[] noticeIds) {
        try {
            noticeService.deleteNotices(noticeIds);
            return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }

    /**
     * 게시판 관리(Notice) - 게시글 수정
     */
    @PatchMapping("/board/notice/{noticeId}")
    public ResponseEntity<String> updateNotice(@PathVariable Long noticeId, boolean noticeYn) {
        try {
            if (noticeService.checkNoticeYN(noticeId, noticeYn)) {
                return ResponseEntity.badRequest().body("이미 공지글로 설정되어있습니다.");
            }
            noticeService.updateNoticeYN(noticeId, noticeYn);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }

    /**
     * 게시판 관리(Q&A) - 조회
     */
    @GetMapping("/board/qa")
    public String getQaList(QaSearchDto qaSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("qaList", qaService.getQaList(qaSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("qaSearchDto", qaSearchDto);
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
            qaService.deleteQas(qaIds);
            return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }

    /**
     * 게시판 관리(Review) - 조회
     */
    @GetMapping("/board/review")
    public String getReviewList(ReviewSearchDto reviewSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("reviews", reviewService.getReviews(reviewSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("reviewSearchDto", reviewSearchDto);
        model.addAttribute("maxPage", 10);
        return "admin/board/review-board-management";
    }

    /**
     * 게시판 관리(Review) - 게시글 삭제
     */
    @DeleteMapping("/board/review")
    public ResponseEntity<String> deleteReview(Long[] reviewIds) {
        try {
            reviewService.deleteReviews(reviewIds);
            return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }
}
