package com.windsome.controller.admin;

import com.windsome.config.security.CurrentMember;
import com.windsome.dto.board.BoardDTO;
import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.board.notice.NoticeUpdateDTO;
import com.windsome.entity.member.Member;
import com.windsome.service.AdminService;
import com.windsome.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/board")
public class AdminBoardController {

    private final BoardService boardService;
    private final AdminService adminService;

    /**
     * 공지 전체 조회
     */
    @GetMapping("/notices")
    public String getNoticeList(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("noticeList", boardService.getNoticeList(searchDTO, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("fixTopNoticeList", boardService.getFixTopNoticeList());
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "admin/board/notice/notice-board-management";
    }

    /**
     * 공지 등록 화면
     */
    @GetMapping("/notices/enroll")
    public String enrollNoticeForm(Model model) {
        model.addAttribute("boardDTO", new BoardDTO());
        return "admin/board/notice/notice-new-post";
    }

    /**
     * 공지 등록
     */
    @PostMapping("/notices/enroll")
    public String enrollNotice(BoardDTO boardDTO, @CurrentMember Member member, RedirectAttributes redirectAttr) {
        adminService.enrollNotice(boardDTO, member);
        redirectAttr.addFlashAttribute("message", "게시글이 등록되었습니다.");
        return "redirect:/admin/board/notices";
    }

    /**
     * 공지 상세 조회
     */
    @GetMapping("/notices/{noticeId}")
    public String noticeDtl(@PathVariable(value = "noticeId") Long noticeId, Optional<Integer> page, Model model, RedirectAttributes redirectAttr) {
        try {
            model.addAttribute("noticeDtlList", adminService.getNoticeDtlList(noticeId));
        } catch (EntityNotFoundException e) {
            redirectAttr.addFlashAttribute("message", "존재하지 않는 게시글입니다.");
            return "redirect:/board/notices";
        }
        model.addAttribute("page", page.orElse(0));
        return "admin/board/notice/notice-detail";
    }

    /**
     * 공지 수정 화면
     */
    @GetMapping("/notices/update/{noticeId}")
    public String updateNoticeForm(@PathVariable(value = "noticeId") Long noticeId, Optional<Integer> page, Model model) {
        try {
            model.addAttribute("noticeDetail", adminService.getNotice(noticeId));
        } catch (EntityNotFoundException e) {
            model.addAttribute("message", "존재하지 않는 게시글입니다.");
            return "admin/board/notice/notice-detail";
        }
        model.addAttribute("page", page.orElse(0));
        return "admin/board/notice/notice-update-post";
    }

    /**
     * 공지 수정
     */
    @PutMapping("/notices/update/{noticeId}")
    public ResponseEntity<String> updateNotice(@PathVariable(value = "noticeId") Long noticeId, BoardDTO boardDTO) {
        try {
            adminService.updateNotice(noticeId, boardDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
        return ResponseEntity.ok().body("게시글이 수정되었습니다.");
    }

    /**
     * 공지 설정 여부 수정
     */
    @PatchMapping("/notices/update/{noticeId}/has-private")
    public ResponseEntity<String> toggleNoticeStatus(@PathVariable Long noticeId, boolean noticeYn) {
        try {
            if (adminService.checkNoticeYN(noticeId, noticeYn)) {
                return ResponseEntity.badRequest().body("이미 공지글로 설정되어있습니다.");
            }
            adminService.updateNoticeYN(noticeId, noticeYn);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }

    /**
     * Q&A 전체 조회
     */
    @GetMapping("/qa")
    public String getQaList(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        model.addAttribute("qaList", boardService.getQaList(searchDTO, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "admin/board/qa/qa-board-management";
    }

    /**
     * Review 전체 조회
     */
    @GetMapping("/reviews")
    public String getReviewList(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        model.addAttribute("reviews", boardService.getReviews(searchDTO, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("maxPage", 10);
        return "admin/board/review/review-board-management";
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deletePosts(@RequestBody Long[] boardIds) {
        try {
            adminService.deletePosts(boardIds);
            return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
    }
}
