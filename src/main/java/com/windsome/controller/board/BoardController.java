package com.windsome.controller.board;

import com.windsome.config.security.CurrentMember;
import com.windsome.dto.board.BoardDTO;
import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.board.notice.*;
import com.windsome.dto.board.qa.QaEnrollDTO;
import com.windsome.dto.board.qa.QaUpdateDTO;
import com.windsome.dto.board.review.*;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Member;
import com.windsome.repository.board.BoardRepository;
import com.windsome.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final BoardRepository boardRepository;

    /**
     * 공지 전체 조회
     */
    @GetMapping("/notices")
    public String notices(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        Page<NoticeListDTO> noticeList = boardService.getNoticeList(searchDTO, pageable);
        List<Board> fixTopNoticeList = boardService.getFixTopNoticeList();

        model.addAttribute("fixTopNoticeList", fixTopNoticeList);
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("searchDTO", new SearchDTO());
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "board/notice/notice-list";
    }

    /**
     * 공지 상세 조회
     */
    @GetMapping("/notices/{noticeId}")
    public String noticeDtl(@PathVariable(value = "noticeId") Long noticeId, Optional<Integer> page, Model model, RedirectAttributes redirectAttr) {
        try {
            model.addAttribute("noticeDtlList", boardService.getNoticeDtlList(noticeId));
        } catch (EntityNotFoundException e) {
            redirectAttr.addFlashAttribute("message", "존재하지 않는 게시글입니다.");
            return "redirect:/board/notices";
        }
        model.addAttribute("page", page.orElse(0));
        return "board/notice/notice-detail";
    }

    /**
     * Q&A 전체 조회
     */
    @GetMapping("/qa")
    public String qa(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);

        model.addAttribute("qaList", boardService.getQaList(searchDTO, pageable));
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "board/qa/qa-list";
    }

    /**
     * Q&A 등록 화면
     */
    @GetMapping("/qa/enroll")
    public String enrollQaForm(@RequestParam(value = "qaId", required = false) Long qaId, Model model) {
        model.addAttribute("qaEnrollDto", new QaEnrollDTO());
        model.addAttribute("qaId", qaId);
        return "board/qa/qa-new-post";
    }

    /**
     * Q&A 등록
     */
    @PostMapping("/qa/enroll")
    public ResponseEntity<String> enrollQa(@CurrentMember Member member, BoardDTO boardDTO) {
        try {
            boardService.enrollQa(boardDTO, member);
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
            Board qa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
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
        if (boardService.validatePost(member, qaId, password)) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }
        return ResponseEntity.ok().body("ok");
    }

    /**
     * Q&A 상세 조회
     */
    @GetMapping("/qa/{qaId}")
    public String qaDtl(@PathVariable(value = "qaId") Long qaId, @RequestParam(required = false) String password,
                        @CurrentMember Member member, Model model, RedirectAttributes redirectAttr) {
        try {
            // 비밀번호 검사를 하지 않고, url을 통해 직접 접근한 경우
            Board qa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            if (password == null) {
                return "redirect:/board/qa/" + qaId + "/password-verification";
            } else if (!password.equals(qa.getPassword())) {
                return "redirect:/board/qa/" + qaId + "/password-verification";
            }

            model.addAttribute("member", member);
            model.addAttribute("qaDtlDtoList", boardService.getQaDtlList(qaId));
            model.addAttribute("commentDtoList", boardService.getCommentList(qaId));
            model.addAttribute("password", password);
        } catch (EntityNotFoundException e) {
            redirectAttr.addFlashAttribute("message", "존재하지 않는 게시글입니다.");
            return "redirect:/board/qa";
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
            Board qa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            if (password == null) {
                return "redirect:/board/qa/" + qaId + "/password-verification";
            } else if (!password.equals(qa.getPassword())) {
                return "redirect:/board/qa/" + qaId + "/password-verification";
            }

            model.addAttribute("qaUpdateDto", boardService.getQaForUpdate(qaId));
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
    public ResponseEntity<String> updateQa(QaUpdateDTO qaUpdateDto) {
        try {
            boardService.updateQa(qaUpdateDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글 입니다.");
        }
        return ResponseEntity.ok().body("게시글이 수정되었습니다.");
    }

    /**
     * Q&A 삭제
     */
    @DeleteMapping("/qa/{qaId}")
    public ResponseEntity<String> deleteQa(@PathVariable(value = "qaId") Long qaId, String password) {
        try {
            if (!boardService.validatePostPassword(qaId, password)) {
                return ResponseEntity.badRequest().body("삭제 권한이 없습니다.");
            }
            boardService.deletePost(qaId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글 입니다.");
        }
        return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
    }

    /**
     * 리뷰 전체 조회
     */
    @GetMapping("/reviews")
    public String review(SearchDTO searchDTO, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        model.addAttribute("reviews", boardService.getReviews(searchDTO, pageable));
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("maxPage", 10);
        return "board/review/review-list";
    }

    /**
     * 리뷰 상세 조회
     */
    @GetMapping("/reviews/{reviewId}")
    public String reviewDtl(@PathVariable(value = "reviewId") Long reviewId, HttpServletRequest request, HttpServletResponse response, Model model) {
        ReviewDtlPageReviewDTO review = boardService.getReviewDtl(reviewId);
        boardService.validateHitsCount(review, request, response);

        model.addAttribute("review", review);
        return "board/review/review-detail";
    }

    /**
     * 리뷰 등록 화면
     */
    @GetMapping("/reviews/enroll")
    public String enrollReviewForm(@RequestParam(value = "productId", required = false) Long productId, Model model) {
        if (productId != null) {
            try {
                ProductDTO review = boardService.getProduct(productId);
                model.addAttribute("productId", productId);
                model.addAttribute("review", review);
            } catch (EntityNotFoundException e) {
                model.addAttribute("message", "상품 정보를 불러오던 도중 오류가 발생하였습니다.");
                return "board/review/review-new-post";
            }
        }
        model.addAttribute("reviewDto", new ReviewEnrollDTO());
        return "board/review/review-new-post";
    }

    /**
     * 리뷰 등록 -> 상품 검색
     */
    @GetMapping("/reviews/product-selection")
    public String showProductSearchPopup(ProductSearchDTO searchDto, Optional<Integer> page, Optional<Integer> size, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(5));

        model.addAttribute("products", boardService.getProductList(searchDto, pageable));
        model.addAttribute("searchQuery", searchDto.getSearchQuery());
        model.addAttribute("size", size.orElse(5));
        model.addAttribute("maxPage", 5);
        return "board/review/product-selection-popup";
    }

    /**
     * 리뷰 등록
     */
    @PostMapping("/reviews/enroll")
    public ResponseEntity<String> enrollReview(@RequestBody BoardDTO boardDTO, @CurrentMember Member member) {
        try {
            boardService.enrollReview(boardDTO, member);
            boardService.setRatingAvg(boardDTO.getProductId());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("리뷰 등록 도중 오류가 발생하였습니다.");
        }
        return ResponseEntity.ok().body("리뷰가 등록되었습니다.");
    }

    /**
     * 리뷰 수정 화면
     */
    @GetMapping("/reviews/update/{reviewId}")
    public String updateReviewForm(@PathVariable(value = "reviewId") Long reviewId, Model model) {
        model.addAttribute("review", boardService.getReviewDtl(reviewId));
        return "board/review/review-update-post";
    }

    /**
     * 리뷰 수정
     */
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable(value = "reviewId") Long reviewId, @RequestBody ReviewUpdateDTO reviewUpdateDto, @CurrentMember Member member) {
        try {
            if (boardService.validateReviewOwnership(reviewId, member)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 리뷰입니다.");
        }

        boardService.updateReview(reviewUpdateDto);
        boardService.setRatingAvg(reviewUpdateDto.getProductId());
        return ResponseEntity.ok().body("리뷰가 수정되었습니다.");
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable(value = "reviewId") Long reviewId, Long productId, @CurrentMember Member member) {
        if (boardService.validateReviewOwnership(reviewId, member)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
        }
        boardService.deletePost(reviewId);
        boardService.setRatingAvg(productId);
        return ResponseEntity.ok().body("리뷰가 삭제되었습니다.");
    }

    /**
     * 리뷰 체크
     */
    @PostMapping("/reviews/check/{productId}")
    public ResponseEntity<String> checkReview(@PathVariable("productId") Long productId, @CurrentMember Member member) {
        if (boardService.checkExistingReview(productId, member.getId())) {
            return ResponseEntity.badRequest().body("이미 등록된 리뷰가 존재합니다.");
        }
        return ResponseEntity.ok().build();
    }
}
