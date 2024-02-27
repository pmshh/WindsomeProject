package com.windsome.service.board;

import com.windsome.dto.board.review.*;
import com.windsome.entity.Member;
import com.windsome.entity.Product;
import com.windsome.entity.ProductImage;
import com.windsome.entity.board.Review;
import com.windsome.repository.board.review.ReviewRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductImageRepository productImageRepository;

    @InjectMocks private ReviewService reviewService;

    @Test
    @DisplayName("리뷰 등록 - 성공")
    public void testEnrollReview_Success() {
        // given
        ReviewEnrollDto reviewEnrollDto = new ReviewEnrollDto();
        reviewEnrollDto.setProductId(1L);
        Member member = new Member();
        Product product = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        reviewService.enrollReview(reviewEnrollDto, member);

        // then
        verify(productRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("리뷰 등록 - 실패 (상품이 존재하지 않는 경우)")
    public void testEnrollReview_EntityExistsException() {
        // given
        ReviewEnrollDto reviewEnrollDto = new ReviewEnrollDto();
        reviewEnrollDto.setProductId(2L);
        Member member = new Member();
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityExistsException.class, () -> {reviewService.enrollReview(reviewEnrollDto, member);});

        // verify
        verify(productRepository, times(1)).findById(2L);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 리스트 조회")
    public void testGetProductList() {
        // given
        ProductSearchDto searchDto = new ProductSearchDto();
        Pageable pageable = Pageable.ofSize(10).withPage(0);

        List<ProductListDto> productList = new ArrayList<>();
        productList.add(new ProductListDto());
        productList.add(new ProductListDto());
        when(productRepository.getReviewPageItemList(any(), any())).thenReturn(productList);
        when(productRepository.getReviewPageItemListCount(any())).thenReturn(2L);

        // when
        PageImpl<ProductListDto> resultPage = reviewService.getProductList(searchDto, pageable);

        // then
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(2, resultPage.getContent().size());
        assertEquals(10, resultPage.getSize());
    }

    @Test
    @DisplayName("리뷰 작성 화면 접근 - 성공")
    public void testGetProduct_Success() {
        // given
        Long itemId = 1L;
        Product product = new Product();
        product.setName("상품1");
        when(productRepository.findById(itemId)).thenReturn(Optional.of(product));

        ProductImage productImage = new ProductImage();
        productImage.setImageUrl("이미지URL");
        when(productImageRepository.findByProductIdAndIsRepresentativeImage(itemId, true)).thenReturn(productImage);

        // when
        ProductDto result = reviewService.getProduct(itemId);

        // then
        assertEquals("상품1", result.getProductName());
        assertEquals("이미지URL", result.getImageUrl());
    }

    @Test
    @DisplayName("리뷰 작성 화면 접근 - 실패 (상품이 존재하지 않는 경우)")
    public void testGetProduct_EntityNotFoundException() {
        // given
        Long itemId = 1L;
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.getProduct(itemId);});
    }

    @Test
    @DisplayName("리뷰 조회 - 성공")
    public void testGetReviewDtl_Success() {
        // given
        Long reviewId = 1L;
        Review review = new Review();
        review.setId(reviewId);
        Product product = new Product();
        product.setId(2L);
        review.setProduct(product);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        ProductImage productImage = new ProductImage();
        productImage.setImageUrl("이미지URL");
        when(productImageRepository.findByProductIdAndIsRepresentativeImage(2L, true)).thenReturn(productImage);

        // when
        ReviewDtlPageReviewDto result = reviewService.getReviewDtl(reviewId);

        // then
        assertEquals(reviewId, result.getReviewId());
        assertEquals("이미지URL", result.getImageUrl());
    }

    @Test
    @DisplayName("리뷰 조회 - 실패 (리뷰가 존재하지 않는 경우)")
    public void testGetReviewDtl_EntityNotFoundException() {
        // given
        Long reviewId = 1L;
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.getReviewDtl(reviewId);});
    }

    @Test
    @DisplayName("리뷰 수정 - 성공")
    public void testUpdateReview_Success() {
        // given
        ReviewUpdateDto reviewUpdateDto = new ReviewUpdateDto();
        reviewUpdateDto.setReviewId(1L);
        reviewUpdateDto.setTitle("수정된 제목");
        Review existingReview = new Review();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existingReview));

        // when
        reviewService.updateReview(reviewUpdateDto);

        // then
        assertEquals(existingReview.getTitle(), "수정된 제목");
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 수정 - 실패 (리뷰가 존재하지 않는 경우)")
    public void testUpdateReview_EntityNotFoundException() {
        // given
        ReviewUpdateDto reviewUpdateDto = new ReviewUpdateDto();
        reviewUpdateDto.setReviewId(1L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.updateReview(reviewUpdateDto);});

        // verify
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("리뷰 삭제 - 성공")
    public void testDeleteReview_Success() {
        // given
        Long reviewId = 1L;

        Review review = new Review();
        review.setId(1L);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        reviewService.deleteReview(reviewId);

        // then
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("리뷰 삭제 - 실패")
    public void testDeleteReview_Fail() {
        // given
        Long reviewId = 1L;

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.deleteReview(reviewId);});
    }

    @Test
    @DisplayName("리뷰 소유권 검증 - 소유자일 경우")
    public void testValidateReviewOwnership_Owner() {
        // given
        Long reviewId = 1L;

        Member member = new Member();
        member.setUserIdentifier("owner");

        Review review = new Review();
        Member reviewOwner = new Member();
        reviewOwner.setUserIdentifier("owner");
        review.setMember(reviewOwner);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        boolean result = reviewService.validateReviewOwnership(reviewId, member);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("리뷰 소유권 검증 - 비소유자일 경우")
    public void testValidateReviewOwnership_NonOwner() {
        // given
        Long reviewId = 1L;

        Member member = new Member();
        member.setUserIdentifier("non_owner");

        Review review = new Review();
        Member reviewOwner = new Member();
        reviewOwner.setUserIdentifier("owner");
        review.setMember(reviewOwner);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        boolean result = reviewService.validateReviewOwnership(reviewId, member);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("리뷰 소유권 검증 - 리뷰가 존재하지 않는 경우")
    public void testValidateReviewOwnership_ReviewNotFound() {
        // given
        Long reviewId = 1L;
        Member member = new Member();
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.validateReviewOwnership(reviewId, member);});
    }

    @Test
    @DisplayName("리뷰 조회")
    public void testGetReviews() {
        // given
        ReviewSearchDto reviewSearchDto = new ReviewSearchDto();
        Pageable pageable = Pageable.ofSize(10).withPage(0);

        List<ReviewListDto> reviewList = new ArrayList<>();
        reviewList.add(new ReviewListDto());
        reviewList.add(new ReviewListDto());

        Page<ReviewListDto> expectedPage = new PageImpl<>(reviewList, pageable, 2);
        when(reviewRepository.getReviews(any(), any())).thenReturn(expectedPage);

        // when
        Page<ReviewListDto> resultPage = reviewService.getReviews(reviewSearchDto, pageable);

        // then
        assertEquals(expectedPage, resultPage);
    }

    @Test
    @DisplayName("리뷰 조회 - 성공")
    public void testGetProductReviewList_Success() {
        // given
        Long productId = 1L;
        Pageable pageable = Pageable.ofSize(10).withPage(0);

        Product product = new Product();
        product.setId(productId);

        List<Review> reviewList = new ArrayList<>();
        Review review1 = new Review();
        review1.setId(1L);
        review1.setProduct(product);
        review1.setRegDate(LocalDateTime.now());
        review1.setMember(new Member());
        reviewList.add(review1);

        when(reviewRepository.findByProductIdOrderByIdDesc(productId, pageable)).thenReturn(reviewList);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.countByProductId(productId)).thenReturn(1L);

        // when
        Page<ProductReviewDTO> resultPage = reviewService.getProductReviewList(productId, pageable);

        // then
        assertEquals(1, resultPage.getTotalElements());
    }

    @Test
    @DisplayName("리뷰 조회 - 실패 (상품이 존재하지 않는 경우)")
    public void testGetProductReviewList_ProductNotFoundException() {
        // given
        Long productId = 1L;
        Pageable pageable = Pageable.ofSize(10).withPage(0);

        Product product = new Product();
        product.setId(productId);

        List<Review> reviewList = new ArrayList<>();
        Review review = new Review();
        review.setProduct(product);
        reviewList.add(review);

        when(reviewRepository.findByProductIdOrderByIdDesc(productId, pageable)).thenReturn(reviewList);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.getProductReviewList(productId, pageable);});
    }

    @Test
    @DisplayName("평균 평점 설정 - 성공")
    public void testSetRatingAvg_Success() {
        // given
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.getRatingAvg(productId)).thenReturn(BigDecimal.valueOf(5));

        // when
        reviewService.setRatingAvg(productId);

        // then
        verify(productRepository, times(1)).findById(productId);
        verify(reviewRepository, times(1)).getRatingAvg(productId);
        assertEquals(BigDecimal.valueOf(5), product.getAverageRating());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("평균 평점 설정 - 실패 (상품이 존재하지 않는 경우)")
    public void testSetRatingAvg_EntityNotFoundException() {
        // given
        Long productId = 1L;
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.setRatingAvg(productId);});

        // verify
        verify(productRepository, times(1)).findById(productId);
        verify(reviewRepository, never()).getRatingAvg(productId);
    }

    @Test
    @DisplayName("리뷰 존재 여부 확인 - 리뷰가 존재하는 경우")
    public void testCheckExistingReview_Exists() {
        // given
        Long productId = 1L;
        Long memberId = 1L;
        when(reviewRepository.existsByProductIdAndMemberId(productId, memberId)).thenReturn(true);

        // when
        boolean exists = reviewService.checkExistingReview(productId, memberId);

        // then
        assertTrue(exists);
    }

    @Test
    @DisplayName("리뷰 존재 여부 확인 - 리뷰가 존재하지 않는 경우")
    public void testCheckExistingReview_NotExists() {
        // given
        Long productId = 1L;
        Long memberId = 1L;
        when(reviewRepository.existsByProductIdAndMemberId(productId, memberId)).thenReturn(false);

        // when
        boolean exists = reviewService.checkExistingReview(productId, memberId);

        // then
        assertFalse(exists);
    }

    @Test
    @DisplayName("조회수 증가 및 쿠키 생성 - 쿠키가 없는 경우")
    public void testValidateHitsCount_NoCookie() {
        // given
        ReviewDtlPageReviewDto reviewDto = new ReviewDtlPageReviewDto();
        reviewDto.setReviewId(1L);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        HttpServletResponse response = mock(HttpServletResponse.class);

        Review findReview = new Review();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(findReview));

        // when
        reviewService.validateHitsCount(reviewDto, request, response);

        // then
        assertEquals(findReview.getHits(), 1);
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(findReview);
        verify(response, times(1)).addCookie(any());
    }

    @Test
    @DisplayName("조회수 증가 및 쿠키 생성 - 쿠키가 있는 경우")
    public void testValidateHitsCount_WithCookie() {
        // given
        ReviewDtlPageReviewDto reviewDto = new ReviewDtlPageReviewDto();
        reviewDto.setReviewId(1L);

        Cookie cookie = new Cookie("checkedReview", "[1]");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        HttpServletResponse response = mock(HttpServletResponse.class);

        Review review = new Review();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // when
        reviewService.validateHitsCount(reviewDto, request, response);

        // then
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(0)).save(review);
        verify(response, times(1)).addCookie(any());
    }

    @Test
    @DisplayName("조회수 증가 및 쿠키 생성 - 리뷰가 존재하지 않는 경우")
    public void testValidateHitsCount_EntityNotFoundException() {
        // given
        ReviewDtlPageReviewDto reviewDto = new ReviewDtlPageReviewDto();
        reviewDto.setReviewId(1L);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.validateHitsCount(reviewDto, request, response);});

        // verify
        verify(reviewRepository, times(1)).findById(1L);
        verify(response, never()).addCookie(any());
    }

    @Test
    @DisplayName("리뷰 삭제 - 성공")
    public void testDeleteReviews_Success() {
        // given
        Long[] reviewIds = {1L, 2L, 3L};

        Review review1 = new Review();
        review1.setId(1L);

        Review review2 = new Review();
        review2.setId(1L);

        Review review3 = new Review();
        review3.setId(1L);

        // 리뷰 ID가 존재하는 상황에서 삭제가 성공했다고 가정
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review1));
        when(reviewRepository.findById(2L)).thenReturn(Optional.of(review2));
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review3));

        // when
        reviewService.deleteReviews(reviewIds);

        // then
        verify(reviewRepository, times(3)).delete(any());
    }

    @Test
    @DisplayName("리뷰 삭제 - 실패 (리뷰가 존재하지 않는 경우)")
    public void testDeleteReviews_EntityNotFoundException() {
        // given
        Long[] reviewIds = {1L, 2L, 3L};

        when(reviewRepository.findById(any())).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {reviewService.deleteReviews(reviewIds);});

        // verify
        verify(reviewRepository, never()).delete(any());
    }
}