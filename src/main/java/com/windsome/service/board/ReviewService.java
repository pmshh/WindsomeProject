package com.windsome.service.board;

import com.windsome.dto.board.review.*;
import com.windsome.entity.board.Review;
import com.windsome.entity.member.Member;
import com.windsome.entity.product.Product;
import com.windsome.entity.product.ProductImage;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.board.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 리뷰 등록
     */
    public void enrollReview(ReviewEnrollDto reviewEnrollDto, Member member){
        Product product = productRepository.findById(reviewEnrollDto.getProductId()).orElseThrow(EntityExistsException::new);
        Review review = Review.createReview(reviewEnrollDto, product, member);
        reviewRepository.save(review);
    }

    /**
     * 리뷰 등록 화면 - 상품 검색(상품 리스트 조회)
     */
    public PageImpl<ProductListDto> getProductList(ProductSearchDto searchDto, Pageable pageable) {
        List<ProductListDto> content = productRepository.getReviewPageItemList(searchDto.getSearchQuery(), pageable);
        Long count = productRepository.getReviewPageItemListCount(searchDto.getSearchQuery());

        return new PageImpl<ProductListDto>(content, pageable, count);
    }

    /**
     * 리뷰 등록 화면 - 상품 상세 화면에서 리뷰 작성 화면 접근 시, 리뷰 등록 화면에 해당 상품 정보 출력
     */
    public ProductDto getProduct(Long itemId) {
        Product product = productRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        ProductImage productImage = productImageRepository.findByProductIdAndIsRepresentativeImage(itemId, true);
        return ProductDto.createProductDto(product, productImage.getImageUrl());
    }

    /**
     * 리뷰 상세 화면 - 리뷰 조회
     */
    public ReviewDtlPageReviewDto getReviewDtl(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        ProductImage productImage = productImageRepository.findByProductIdAndIsRepresentativeImage(review.getProduct().getId(), true);
        return ReviewDtlPageReviewDto.createReviewDtlPageDto(review, productImage);
    }

    /**
     * 리뷰 수정
     */
    public void updateReview(ReviewUpdateDto reviewUpdateDto) {
        Review findReview = reviewRepository.findById(reviewUpdateDto.getReviewId()).orElseThrow(EntityNotFoundException::new);
        findReview.updateReview(reviewUpdateDto);
        reviewRepository.save(findReview);
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        reviewRepository.delete(review);
    }

    /**
     * 리뷰 수정/삭제 권한 검증
     */
    public boolean validateReviewOwnership(Long reviewId, Member member) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        return !StringUtils.equals(member.getUserIdentifier(), review.getMember().getUserIdentifier());
    }

    /**
     * 리뷰 게시판 - 리뷰 조회
     */
    public Page<ReviewListDto> getReviews(ReviewSearchDto reviewSearchDto, Pageable pageable) {
        return reviewRepository.getReviews(reviewSearchDto, pageable);
    }

    /**
     * 상품 상세 화면 - 리뷰 조회
     */
    public Page<ProductReviewDTO> getProductReviewList(Long productId, Pageable pageable) {
        List<Review> content = reviewRepository.findByProductIdOrderByIdDesc(productId, pageable);

        List<ProductReviewDTO> productReviewDTOList = new ArrayList<>();
        for (Review review : content) {
            Product product = productRepository.findById(review.getProduct().getId()).orElseThrow(EntityNotFoundException::new);

            ProductReviewDTO productReviewDTO = ProductReviewDTO.createProductReviewDTO(review, product);
            productReviewDTOList.add(productReviewDTO);
        }
        Long totalCount = reviewRepository.countByProductId(productId);

        return new PageImpl<ProductReviewDTO>(productReviewDTOList, pageable, totalCount);
    }

    /**
     * 상품 리뷰 평균 평점
     */
    public void setRatingAvg(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        product.setAverageRating(reviewRepository.getRatingAvg(productId));
        productRepository.save(product);
    }

    /**
     * 리뷰 존재 여부 반환
     */
    public boolean checkExistingReview(Long productId, Long memberId) {
        return reviewRepository.existsByProductIdAndMemberId(productId, memberId);
    }

    /**
     * 리뷰 조회수
     */
    public void validateHitsCount(ReviewDtlPageReviewDto review, HttpServletRequest request, HttpServletResponse response) {
        Review findReview = reviewRepository.findById(review.getReviewId()).orElseThrow(EntityNotFoundException::new);

        Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElseGet(() -> new Cookie[0]);

        // "checkedReview" 쿠키가 있을 시, 변수 cookie에 해당 쿠키 추가
        Cookie cookie = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("checkedReview"))
                .findFirst()
                .orElseGet(() -> {
                    findReview.addHitsCount();
                    reviewRepository.save(findReview);
                    return new Cookie("checkedReview", "[" + review.getReviewId() + "]");
                });

        // "checkedReview" 쿠키가 없을 시, 조회수 증가 및 "reviewHits" 쿠키 새로 생성
        if (!cookie.getValue().contains("[" + review.getReviewId() + "]")) {
            findReview.addHitsCount();
            reviewRepository.save(findReview);
            cookie.setValue(cookie.getValue() + "[" + review.getReviewId() + "]");
        }

        long todayEndSecond = LocalDate.now().atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
        long currentSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge((int) (todayEndSecond - currentSecond)); // 오늘 하루 자정까지 남은 시간초 설정
        response.addCookie(cookie);
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReviews(Long[] reviewIds) {
        for (Long reviewId : reviewIds) {
            Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
            reviewRepository.delete(review);
        }
    }
}
