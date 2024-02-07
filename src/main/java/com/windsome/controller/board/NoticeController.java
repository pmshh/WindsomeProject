package com.windsome.controller.board;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.board.notice.*;
import com.windsome.entity.Account;
import com.windsome.entity.board.Notice;
import com.windsome.service.board.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 게시판 화면
     */
    @GetMapping("/notice")
    public String notice(NoticeSearchDto noticeSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        Page<NoticeListDto> noticeList = noticeService.getNoticeList(noticeSearchDto, pageable);
        List<Notice> fixTopNoticeList = noticeService.getFixTopNoticeList();

        model.addAttribute("fixTopNoticeList", fixTopNoticeList);
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("noticeSearchDto", new NoticeSearchDto());
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "board/notice/notice";
    }

    /**
     * 공지사항 등록 화면
     */
    @GetMapping("/notice/enroll")
    public String enrollNoticeForm(Model model) {
        model.addAttribute("noticeDto", new NoticeDto());
        return "board/notice/noticeEnroll";
    }

    /**
     * 공지사항 등록
     */
    @PostMapping("/notice/enroll")
    public String enrollNotice(NoticeDto noticeDto, @CurrentAccount Account account, RedirectAttributes redirectAttr, Model model) {
        if (!noticeService.isAdmin(account)) {
            model.addAttribute("message", "작성 권한이 없습니다.");
            return "board/notice/noticeEnroll";
        }
        noticeService.enrollNotice(noticeDto, account);
        redirectAttr.addFlashAttribute("message", "게시글이 등록되었습니다.");
        return "redirect:/board/notice";
    }

    /**
     * 공지사항 상세 화면
     */
    @GetMapping("/notice/{noticeId}")
    public String noticeDtl(@PathVariable(value = "noticeId") Long noticeId, Integer page, Model model) {
        List<NoticeDtlDto> noticeDtlList = noticeService.getNoticeDtl(noticeId);
        model.addAttribute("noticeDtlList", noticeDtlList);
        model.addAttribute("page", page);
        return "board/notice/noticeDtl";
    }

    /**
     * 공지사항 수정 화면
     */
    @GetMapping("/notice/update/{noticeId}")
    public String updateNoticeForm(@PathVariable(value = "noticeId") Long noticeId, Integer page, Model model) {
        try {
            Notice notice = noticeService.getNotice(noticeId);
            model.addAttribute("notice", notice);
        } catch (EntityNotFoundException e) {
            model.addAttribute("message", "존재하지 않는 게시글입니다.");
            return "board/notice/noticeDtl";
        }
        model.addAttribute("page", page);
        return "board/notice/noticeUpdate";
    }

    /**
     * 공지사항 수정
     */
    @PatchMapping("/notice/{noticeId}")
    public ResponseEntity<String> updateNotice(@PathVariable(value = "noticeId") Long noticeId, NoticeUpdateDto noticeUpdateDto) {
        try {
            noticeService.updateNotice(noticeId, noticeUpdateDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");

        }
        return ResponseEntity.ok().body("게시글이 수정되었습니다.");
    }

    /**
     * 공지사항 삭제
     */
    @DeleteMapping("/notice/{noticeId}")
    public ResponseEntity<String> deleteNotice(@PathVariable(value = "noticeId") Long noticeId, @CurrentAccount Account account) {
        if (!noticeService.isAdmin(account)) {
            return ResponseEntity.badRequest().body("삭제 권한이 없습니다.");
        }

        try {
            noticeService.deleteNotice(noticeId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("잘못된 접근입니다.");
        }
        return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
    }
}
