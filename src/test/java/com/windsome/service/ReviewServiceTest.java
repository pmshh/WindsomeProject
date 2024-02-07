package com.windsome.service;

import com.windsome.WithAccount;
import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.board.review.*;
import com.windsome.entity.Account;
import com.windsome.entity.Item;
import com.windsome.entity.ItemImg;
import com.windsome.entity.board.Review;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.board.review.ReviewRepository;
import com.windsome.service.board.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ReviewServiceTest {

    @Autowired
    ReviewService reviewService;
    @Autowired ReviewRepository reviewRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired ItemImgRepository itemImgRepository;
    @Autowired AccountRepository accountRepository;

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 테스트")
    @Test
    public void enrollReviewTest() {
        // given
        Item item = createItem(1);
        Account account = accountRepository.findByUserIdentifier("test1234");
        ReviewEnrollDto reviewEnrollDto = ReviewEnrollDto.builder()
                .itemId(item.getId())
                .title("test")
                .content("test")
                .rating(BigDecimal.valueOf(5))
                .password("test")
                .build();

        // when
        reviewService.enrollReview(reviewEnrollDto, account);
        Review review = reviewRepository.findByItemId(item.getId());

        // then
        assertNotNull(review);
        assertEquals(review.getTitle(), "test");
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 화면 - 상품 검색(상품 리스트 조회) 테스트")
    @Test
    public void getItemList() {
        // given
        for (int i = 0; i < 10; i++) {
            createItem(i);
        }
        Pageable pageable = PageRequest.of(0, 5);

        // when
        PageImpl<ItemListDto> itemList = reviewService.getItemList(new ItemSearchDto("테스트 상품"), pageable);

        // then
        assertEquals(itemList.getTotalElements(), 10);
        assertEquals(itemList.getSize(), 5);
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 화면 - 상품 상세 정보 조회 테스트")
    @Test
    public void getItem() {
        // given
        Item item = createItem(1);

        // when
        ItemDto itemDto = reviewService.getItem(item.getId());

        // then
        assertEquals(itemDto.getItemId(), item.getId());
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 상세 화면 - 리뷰 조회")
    @Test
    public void getReviewDtl() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        Item item = createItem(1);
        Review review = createReview(item, account);
        ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(item.getId(), "Y");

        // when
        ReviewDtlPageReviewDto reviewDto = reviewService.getReviewDtl(review.getId());

        // then
        assertEquals(reviewDto.getImgUrl(), itemImg.getImgUrl());
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정 테스트")
    @Test
    public void updateReview() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        Item item = createItem(1);
        Review review = createReview(item, account);
        ReviewUpdateDto reviewUpdateDto = ReviewUpdateDto.builder()
                .reviewId(review.getId())
                .title("제목 수정")
                .content("내용 수정")
                .rating(BigDecimal.valueOf(3))
                .build();

        // when
        reviewService.updateReview(reviewUpdateDto);
        Review updatedReview = reviewRepository.findById(review.getId()).orElseThrow(EntityNotFoundException::new);

        // then
        assertEquals(updatedReview.getTitle(), "제목 수정");
        assertEquals(updatedReview.getContent(), "내용 수정");
        assertEquals(updatedReview.getRating(), BigDecimal.valueOf(3));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 삭제 테스트")
    @Test
    public void deleteReview() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        Item item = createItem(1);
        Review review = createReview(item, account);

        // when
        reviewService.deleteReview(review.getId());

        // then
        assertThrows(EntityNotFoundException.class, () -> {
            reviewRepository.findById(review.getId()).orElseThrow(EntityNotFoundException::new);
        });
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정/삭제 권한 검증 테스트 - 성공")
    @Test
    public void validateReviewSuccess() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        Review review = createReview(createItem(1), account);

        // when
        boolean result = reviewService.validateReview(review.getId(), account.getUserIdentifier());

        // then
        assertFalse(result);
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정/삭제 권한 검증 테스트 - 실패")
    @Test
    public void validateReviewFail() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        Review review = createReview(createItem(1), account);
        Account newAccount = createAccount();

        // when
        boolean result = reviewService.validateReview(review.getId(), newAccount.getUserIdentifier());

        // then
        assertTrue(result);
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 게시판 - 리뷰 조회 테스트")
    @Test
    public void getReviews() {
        // given
        Account account = createAccount();
        for (int i = 0; i < 10; i++) {
            createReview(createItem(i), account);
        }

        // when
        Page<ReviewListDto> reviews = reviewService.getReviews(
                new ReviewSearchDto("title", "테스트 상품")
                , PageRequest.of(0, 5));

        // then
        assertEquals(reviews.getTotalElements(), 10);
        assertEquals(reviews.getSize(), 5);
    }

    @WithAccount("test1234")
    @DisplayName("상품 상세 화면 - 리뷰 조회 테스트")
    @Test
    public void getItemDtlPageReviews() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        Item item = createItem(1);
        for (int i = 0; i < 10; i++) {
            createReview(item, account);
        }
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<ItemDtlPageReviewDto> itemDtlPageReviews = reviewService.getItemDtlPageReviews(item.getId(), pageable);

        // then
        assertEquals(itemDtlPageReviews.getTotalElements(), 10);
        assertEquals(itemDtlPageReviews.getSize(), 5);
    }

    public Review createReview(Item item, Account account) {
        Review review = Review.builder()
                .item(item)
                .account(account)
                .content("test")
                .title(item.getItemNm() + " 리뷰")
                .password("test")
                .rating(BigDecimal.valueOf(5))
                .regDate(LocalDateTime.now())
                .hits(0)
                .build();
        return reviewRepository.save(review);
    }

    public Item createItem(int num) {
        Item item = Item.builder()
                .itemNm("테스트 상품 " + num)
                .price(10000)
                .discount(0)
                .itemDetail("테스트 상품 상세 설명")
                .itemSellStatus(ItemSellStatus.SELL)
                .stockNumber(100)
                .build();
        itemRepository.save(item);

        ItemImg itemImg = ItemImg.builder()
                .item(item)
                .oriImgName("test")
                .imgName("test")
                .imgUrl("test")
                .repImgYn("Y")
                .build();
        itemImgRepository.save(itemImg);
        return item;
    }

    public Account createAccount() {
        Account account = Account.builder()
                .userIdentifier("gildong123")
                .password("gildong123")
                .name("gildong")
                .email("gildong@naver.com")
                .address1("test")
                .address2("test")
                .address3("test")
                .build();
        return accountRepository.save(account);
    }
}