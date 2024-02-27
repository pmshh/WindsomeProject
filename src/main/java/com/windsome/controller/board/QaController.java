package com.windsome.controller.board;

import com.windsome.config.security.CurrentMember;
import com.windsome.dto.board.qa.*;
import com.windsome.entity.Member;
import com.windsome.entity.board.Qa;
import com.windsome.repository.board.qa.QaRepository;
import com.windsome.service.board.QaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class QaController {

    private final QaService qaService;
    private final QaRepository qaRepository;

    /**
     * Q&A 게시판 화면
     */
    @GetMapping("/qa")
    public String qa(QaSearchDto qaSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);

        model.addAttribute("qaList", qaService.getQaList(qaSearchDto, pageable));
        model.addAttribute("qaSearchDto", qaSearchDto);
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "board/qa/qa-list";
    }

    /**
     * Q&A 등록 화면
     */
    @GetMapping("/qa/enroll")
    public String enrollQaForm(@RequestParam(value = "qaId", required = false) Long qaId, Model model) {
        model.addAttribute("qaEnrollDto", new QaEnrollDto());
        model.addAttribute("qaId", qaId);
        return "board/qa/qa-new-post";
    }

    /**
     * Q&A 등록
     */
    @PostMapping("/qa/enroll")
    public ResponseEntity<String> enrollQa(@CurrentMember Member member, QaEnrollDto qaEnrollDto) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }
        try {
            qaService.enrollQa(qaEnrollDto, member);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("게시글을 등록하던 도중 오류가 발생하였습니다.");
        }
        return ResponseEntity.ok().body("게시글이 등록되었습니다.");
    }

    /**
     * Q&A 비밀글 - 비밀번호 입력 화면
     */
    @GetMapping("/qa/{qaId}/password-verification")
    public String secretForm(@PathVariable(value = "qaId") Long qaId, Model model) {
        model.addAttribute("qaId", qaId);
        try {
            Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            model.addAttribute("password", qa.getPassword());
        } catch (EntityNotFoundException e) {
            model.addAttribute("message", "존재하지 않는 게시글입니다.");
            return "board/qa/qa-secret-post-verification";
        }
        return "board/qa/qa-secret-post-verification";
    }

    /**
     * Q&A 비밀글 - 비밀번호 검증
     */
    @PostMapping("/qa/{qaId}/password-verification")
    public ResponseEntity<String> secret(@CurrentMember Member member, @PathVariable(value = "qaId") Long qaId, String password) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }
        if (qaService.validatePost(member, qaId, password)) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }
        return ResponseEntity.ok().body("ok");
    }

    /**
     * Q&A 상세 화면
     */
    @GetMapping("/qa/{qaId}")
    public String qaDtl(@PathVariable(value = "qaId") Long qaId, @RequestParam(required = false) String password, @CurrentMember Member member, Model model) {
        try {
            // 비밀번호 검사를 하지 않고, url을 통해 직접 접근한 경우
            Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            if (password == null) {
                return "redirect:/board/qa/" + qaId + "/password-verification";
            } else if (!password.equals(qa.getPassword())) {
                return "redirect:/board/qa/" + qaId + "/password-verification";
            }

            model.addAttribute("member", member);
            model.addAttribute("commentDtoList", qaService.getCommentList(qaId));
            model.addAttribute("qaDtlDtoList", qaService.getQaDtl(qaId));
            model.addAttribute("password", password);
        } catch (EntityNotFoundException e) {
            model.addAttribute("message", "존재하지 않는 게시글입니다.");
            return "board/qa/qa-detail";
        }
        return "board/qa/qa-detail";
    }

    /**
     * Q&A 수정 화면
     */
    @GetMapping("/qa/update/{qaId}")
    public String updateQaForm(@PathVariable(value = "qaId") Long qaId, String password, Model model) {
        try {
            // 비밀번호를 입력 하지 않고, url을 통해 직접 접근한 경우
            Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            if (password == null) {
                return "redirect:/board/qa/" + qaId + "/password-verification";
            } else if (!password.equals(qa.getPassword())) {
                return "redirect:/board/qa/" + qaId + "/password-verification";
            }

            model.addAttribute("qaUpdateDto", qaService.getQaForUpdate(qaId));
        } catch (EntityNotFoundException e) {
            model.addAttribute("message", "존재하지 않는 게시글입니다.");
            return "board/qa/qa-update-post";
        }
        return "board/qa/qa-update-post";
    }

    /**
     * Q&A 수정
     */
    @PatchMapping("/qa/{qaId}")
    public ResponseEntity<String> updateQa(@CurrentMember Member member, QaUpdateDto qaUpdateDto, Long qaId) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }

        try {
            qaService.updateQa(qaUpdateDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글 입니다.");
        }
        return ResponseEntity.ok().body("게시글이 수정되었습니다.");
    }

    /**
     * Q&A 삭제
     */
    @DeleteMapping("/qa/{qaId}")
    public ResponseEntity<String> deleteQa(@CurrentMember Member member, Long qaId, String password) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }

        try {
            if (!qaService.validatePostPassword(qaId, password)) {
                return ResponseEntity.badRequest().body("삭제 권한이 없습니다.");
            }
            qaService.deleteQa(qaId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글 입니다.");
        }
        return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
    }
}
