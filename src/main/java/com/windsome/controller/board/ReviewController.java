package com.windsome.controller.board;

import com.windsome.config.security.CurrentMember;
import com.windsome.dto.board.review.*;
import com.windsome.entity.Member;
import com.windsome.service.CartService;
import com.windsome.service.board.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 게시판 화면
     */
    @GetMapping("/review")
    public String review(ReviewSearchDto reviewSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        model.addAttribute("reviews", reviewService.getReviews(reviewSearchDto, pageable));
        model.addAttribute("reviewSearchDto", reviewSearchDto);
        model.addAttribute("maxPage", 10);
        return "board/review/review-list";
    }

    /**
     * 리뷰 상세 화면
     */
    @GetMapping("/review/{reviewId}")
    public String reviewDtl(@PathVariable(value = "reviewId") Long reviewId, HttpServletRequest request, HttpServletResponse response, Model model) {
        ReviewDtlPageReviewDto review = reviewService.getReviewDtl(reviewId);
        reviewService.validateHitsCount(review, request, response);

        model.addAttribute("review", review);
        return "board/review/review-detail";
    }

    /**
     * 리뷰 등록 화면
     */
    @GetMapping("/review/enroll")
    public String enrollReviewForm(@CurrentMember Member member, RedirectAttributes redirectAttr, @RequestParam(value = "productId", required = false) Long productId, Model model) {
        if (member == null) {
            redirectAttr.addFlashAttribute("message", "로그인 후 이용해주세요.");
            return "redirect:/board/review";
        }

        if (productId != null) {
            ProductDto review = reviewService.getProduct(productId);
            model.addAttribute("productId", productId);
            model.addAttribute("review", review);
        }
        model.addAttribute("reviewDto", new ReviewEnrollDto());
        return "board/review/review-new-post";
    }

    /**
     * 리뷰 등록 -> 상품 검색
     */
    @GetMapping("/review/product-selection")
    public String showProductSearchPopup(ProductSearchDto searchDto, Optional<Integer> page, Optional<Integer> size, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(5));

        model.addAttribute("products", reviewService.getProductList(searchDto, pageable));
        model.addAttribute("searchQuery", searchDto.getSearchQuery());
        model.addAttribute("size", size.orElse(5));
        model.addAttribute("maxPage", 5);
        return "board/review/product-selection-popup";
    }

    /**
     * 리뷰 등록
     */
    @PostMapping("/review/enroll")
    public ResponseEntity<String> enrollReview(@RequestBody ReviewEnrollDto reviewEnrollDto, @CurrentMember Member member) {
        try {
            reviewService.enrollReview(reviewEnrollDto, member);
            reviewService.setRatingAvg(reviewEnrollDto.getProductId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("리뷰 등록 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.ok().body("리뷰가 등록되었습니다.");
    }

    /**
     * 리뷰 수정 화면
     */
    @GetMapping("/review/update/{reviewId}")
    public String updateReviewForm(@PathVariable(value = "reviewId") Long reviewId, Model model) {
        model.addAttribute("review", reviewService.getReviewDtl(reviewId));
        return "board/review/review-update-post";
    }

    /**
     * 리뷰 수정
     */
    @PatchMapping("/review/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable(value = "reviewId") Long reviewId, @RequestBody ReviewUpdateDto reviewUpdateDto, @CurrentMember Member member) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해주세요.");
        }

        try {
            if (reviewService.validateReviewOwnership(reviewId, member)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 리뷰입니다.");
        }

        reviewService.updateReview(reviewUpdateDto);
        reviewService.setRatingAvg(reviewUpdateDto.getProductId());
        return ResponseEntity.ok().body("리뷰가 수정되었습니다.");
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable(value = "reviewId") Long reviewId, Long productId, @CurrentMember Member member) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 이용해주세요.");
        }
        if (reviewService.validateReviewOwnership(reviewId, member)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
        }
        reviewService.deleteReview(reviewId);
        reviewService.setRatingAvg(productId);
        return ResponseEntity.ok().body("리뷰가 삭제되었습니다.");
    }

    /**
     * 리뷰 체크
     */
    @PostMapping("/review/check/{productId}")
    public ResponseEntity<String> checkReview(@PathVariable("productId") Long productId, @CurrentMember Member member) {
        if (reviewService.checkExistingReview(productId, member.getId())) {
            return ResponseEntity.badRequest().body("이미 등록된 리뷰가 존재합니다.");
        }
        return ResponseEntity.ok().build();
    }
}
