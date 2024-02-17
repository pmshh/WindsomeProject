package com.windsome.controller.board;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.board.qa.*;
import com.windsome.entity.Account;
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
        return "/board/qa/qa";
    }

    /**
     * Q&A 등록 화면
     */
    @GetMapping("/qa/enroll")
    public String enrollQaForm(@RequestParam(value = "qaId", required = false) Long qaId, Model model) {
        model.addAttribute("qaEnrollDto", new QaEnrollDto());
        model.addAttribute("qaId", qaId);
        return "/board/qa/qaEnroll";
    }

    /**
     * Q&A 등록
     */
    @PostMapping("/qa/enroll")
    public ResponseEntity<String> enrollQa(@CurrentAccount Account account, QaEnrollDto qaEnrollDto) {
        if (account == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }
        try {
            qaService.enrollQa(qaEnrollDto, account);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("게시글을 등록하던 도중 오류가 발생하였습니다.");
        }
        return ResponseEntity.ok().body("게시글이 등록되었습니다.");
    }

    /**
     * Q&A 비밀글 - 비밀번호 입력 화면
     */
    @GetMapping("/qa/secret")
    public String secretForm(Long qaId, Model model) {
        model.addAttribute("qaId", qaId);
        try {
            Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            model.addAttribute("password", qa.getPassword());
        } catch (EntityNotFoundException e) {
            model.addAttribute("message", "존재하지 않는 게시글입니다.");
            return "/board/qa/qaSecret";
        }
        return "/board/qa/qaSecret";
    }

    /**
     * Q&A 비밀글 - 비밀번호 입력
     */
    @PostMapping("/qa/secret")
    public ResponseEntity<String> secret(@CurrentAccount Account account, Long qaId, String password) {
        if (account == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해 주세요.");
        }
        if (!qaService.validatePost(account, qaId, password)) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }
        return ResponseEntity.ok().body("ok");
    }

    /**
     * Q&A 상세 화면
     */
    @GetMapping("/qa/{qaId}")
    public String qaDtl(@PathVariable(value = "qaId") Long qaId, @RequestParam(required = false) String password, @CurrentAccount Account account, Model model) {
        try {
            // 비밀번호 검사를 하지 않고, url을 통해 직접 접근한 경우
            Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            if (password == null) {
                return "redirect:/board/qa/secret?qaId=" + qaId;
            } else if (!password.equals(qa.getPassword())) {
                return "redirect:/board/qa/secret?qaId=" + qaId;
            }

            model.addAttribute("account", account);
            model.addAttribute("commentDtoList", qaService.getCommentList(qaId));
            model.addAttribute("qaDtlDtoList", qaService.getQaDtl(qaId));
            model.addAttribute("password", password);
        } catch (EntityNotFoundException e) {
            model.addAttribute("message", "존재하지 않는 게시글입니다.");
            return "/board/qa/qaDtl";
        }
        return "/board/qa/qaDtl";
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
                return "redirect:/board/qa/secret?qaId=" + qaId;
            } else if (!password.equals(qa.getPassword())) {
                return "redirect:/board/qa/secret?qaId=" + qaId;
            }

            model.addAttribute("qaUpdateDto", qaService.getQaForUpdate(qaId));
        } catch (EntityNotFoundException e) {
            model.addAttribute("message", "존재하지 않는 게시글입니다.");
            return "/board/qa/qaUpdate";
        }
        return "/board/qa/qaUpdate";
    }

    /**
     * Q&A 수정
     */
    @PatchMapping("/qa/{qaId}")
    public ResponseEntity<String> updateQa(@CurrentAccount Account account, QaUpdateDto qaUpdateDto, Long qaId) {
        if (account == null) {
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
    public ResponseEntity<String> deleteQa(@CurrentAccount Account account, Long qaId, String password) {
        if (account == null) {
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
