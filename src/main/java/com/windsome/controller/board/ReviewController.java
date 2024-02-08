package com.windsome.controller.board;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.board.review.*;
import com.windsome.entity.Account;
import com.windsome.service.CartService;
import com.windsome.service.board.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final CartService cartService;

    /**
     * 리뷰 게시판 화면
     */
    @GetMapping("/review")
    public String review(@CurrentAccount Account account, ReviewSearchDto reviewSearchDto, Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        Page<ReviewListDto> reviews = reviewService.getReviews(reviewSearchDto, pageable);

        Long cartItemTotalCount = null;
        if (account != null) {
            cartItemTotalCount = cartService.getCartItemTotalCount(account);
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewSearchDto", reviewSearchDto);
        model.addAttribute("cartItemTotalCount", cartItemTotalCount);
        model.addAttribute("maxPage", 10);
        return "board/review/review";
    }

    /**
     * 리뷰 상세 화면
     */
    @GetMapping("/review/{reviewId}")
    public String reviewDtl(@PathVariable(value = "reviewId") Long reviewId, HttpServletRequest request, HttpServletResponse response, Model model) {
        ReviewDtlPageReviewDto review = reviewService.getReviewDtl(reviewId);
        reviewService.validateHitsCount(review, request, response);

        model.addAttribute("review", review);
        return "board/review/reviewDtl";
    }

    /**
     * 리뷰 등록 화면
     */
    @GetMapping("/review/enroll")
    public String enrollReviewForm(@RequestParam(value = "itemId", required = false) Long itemId, Model model) {
        if (itemId != null) {
            ItemDto review = reviewService.getItem(itemId);
            model.addAttribute("itemId", itemId);
            model.addAttribute("review", review);
        }
        model.addAttribute("reviewDto", new ReviewEnrollDto());
        return "board/review/reviewEnroll";
    }

    /**
     * 리뷰 등록 -> 상품 검색
     */
    @GetMapping("/review/itemList")
    public String getItemList(ItemSearchDto searchDto, Optional<Integer> page, Optional<Integer> size, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(5));

        model.addAttribute("items", reviewService.getItemList(searchDto, pageable));
        model.addAttribute("searchQuery", searchDto.getSearchQuery());
        model.addAttribute("size", size.orElse(5));
        model.addAttribute("maxPage", 5);
        return "board/review/itemList";
    }

    /**
     * 리뷰 등록
     */
    @PostMapping("/review/enroll")
    public ResponseEntity<String> enrollReview(@RequestBody ReviewEnrollDto reviewEnrollDto, @CurrentAccount Account account) {
        try {
            reviewService.enrollReview(reviewEnrollDto, account);
            reviewService.setRatingAvg(reviewEnrollDto.getItemId());
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
        return "board/review/reviewUpdate";
    }

    /**
     * 리뷰 수정
     */
    @PatchMapping("/review/update/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable(value = "reviewId") Long reviewId, @RequestBody ReviewUpdateDto reviewUpdateDto, @CurrentAccount Account account) {
        if (reviewService.validateReview(reviewId, account.getUserIdentifier())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
        }
        reviewService.updateReview(reviewUpdateDto);
        reviewService.setRatingAvg(reviewUpdateDto.getItemId());
        return ResponseEntity.ok().body("리뷰가 수정되었습니다.");
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/review/delete/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable(value = "reviewId") Long reviewId, Long itemId, @CurrentAccount Account account) {
        if (reviewService.validateReview(reviewId, account.getUserIdentifier())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
        }
        reviewService.deleteReview(reviewId);
        reviewService.setRatingAvg(itemId);
        return ResponseEntity.ok().body("리뷰가 삭제되었습니다.");
    }

    /**
     * 리뷰 체크
     */
    @PostMapping("/review/check/{itemId}")
    public ResponseEntity<String> checkReview(@PathVariable("itemId") Long itemId, @CurrentAccount Account account) {
        if (reviewService.existsByItemIdAndAccountId(itemId, account.getId())) {
            return ResponseEntity.badRequest().body("이미 등록된 리뷰가 존재합니다.");
        }
        return ResponseEntity.ok().build();
    }
}
