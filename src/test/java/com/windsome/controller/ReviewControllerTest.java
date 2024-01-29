package com.windsome.controller;

import com.windsome.WithAccount;
import com.windsome.constant.ItemSellStatus;
import com.windsome.entity.Account;
import com.windsome.entity.Item;
import com.windsome.entity.ItemImg;
import com.windsome.entity.Review;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.ReviewRepository;
import com.windsome.service.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ReviewControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReviewController reviewController;
    @Autowired ReviewService reviewService;
    @Autowired ReviewRepository reviewRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired ItemImgRepository itemImgRepository;

    @AfterEach
    void afterEach() {
        itemImgRepository.deleteAll();
        itemRepository.deleteAll();
        accountRepository.deleteAll();
        reviewRepository.deleteAll();
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 게시판 화면 보이는지 테스트")
    @Test
    void reviews() throws Exception {
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reviewSearchDto"))
                .andExpect(model().attributeExists("cartItemTotalCount"))
                .andExpect(model().attributeExists("maxPage"))
                .andExpect(view().name("review/reviews"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 상세 화면 보이는지 테스트")
    @Test
    void reviewDtl() throws Exception {
        Review review = createReview(createItem(), createAccount());

        mockMvc.perform(get("/review/" + review.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("review"))
                .andExpect(view().name("review/reviewDtl"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 화면 보이는지 테스트")
    @Test
    void enrollReviewForm() throws Exception {
        mockMvc.perform(get("/review/enroll"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reviewDto"))
                .andExpect(view().name("review/reviewEnroll"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 - 상품 검색 테스트")
    @Test
    void getItemList() throws Exception {
        createItem();

        mockMvc.perform(get("/review/itemList")
                        .param("searchQuery", "test"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("searchQuery"))
                .andExpect(model().attributeExists("size"))
                .andExpect(model().attributeExists("maxPage"))
                .andExpect(view().name("review/itemList"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 테스트")
    @Test
    void enrollReview() throws Exception {
        Item item = createItem();

        String requestJson = "{\"itemId\":\"" + item.getId() + "\", \"title\": \"test1234\"" +
                ", \"rating\": \"5\", \"content\": \"test1234\", \"password\": \"test1234\"}";

        mockMvc.perform(post("/review/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("리뷰가 등록되었습니다."));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 테스트 - 실패 (db에 존재하지 않는 itemId 전송)")
    @Test
    void enrollReviewFail() throws Exception {
        Item item = createItem();

        String requestJson = "{\"itemId\":\"" + (item.getId() + 1) + "\", \"title\": \"test1234\"" +
                ", \"rating\": \"5\", \"content\": \"test1234\", \"password\": \"test1234\"}";

        mockMvc.perform(post("/review/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("리뷰 등록 중 오류가 발생하였습니다."));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정 화면 보이는지 테스트")
    @Test
    void updateReviewForm() throws Exception {
        Review review = createReview(createItem(), createAccount());

        mockMvc.perform(get("/review/update/" + review.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("review"))
                .andExpect(view().name("review/reviewUpdate"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정 테스트 - 권한 있는 사용자")
    @Test
    void updateReview() throws Exception {
        Review review = createReview(createItem(), accountRepository.findByUserIdentifier("test1234"));

        String requestJson = "{\"reviewId\":\"" + review.getId() + "\", \"title\": \"test1234\"" +
                ", \"content\": \"test1234\", \"password\": \"12341234\", \"rating\": \"5\"}";

        mockMvc.perform(patch("/review/update/" + review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("리뷰가 수정되었습니다."));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정 테스트 - 권한 없는 사용자")
    @Test
    void updateReviewFail() throws Exception {
        Review review = createReview(createItem(), createAccount());

        String requestJson = "{\"reviewId\":\"" + review.getId() + "\", \"title\": \"test1234\"" +
                ", \"content\": \"test1234\", \"password\": \"12341234\", \"rating\": \"5\"}";

        mockMvc.perform(patch("/review/update/" + review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("수정 권한이 없습니다."));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 삭제 테스트 - 권한 있는 사용자")
    @Test
    void deleteReviewSuccess() throws Exception {
        Account account = accountRepository.findByUserIdentifier("test1234");
        Review review = createReview(createItem(), account);

        mockMvc.perform(delete("/review/delete/" + review.getId())
                        .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("리뷰가 삭제되었습니다."));;
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 삭제 테스트 - 권한 없는 사용자")
    @Test
    void deleteReviewFail() throws Exception {
        Review review = createReview(createItem(), createAccount());

        mockMvc.perform(delete("/review/delete/" + review.getId())
                        .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("삭제 권한이 없습니다."));
    }

    public Review createReview(Item item, Account account) {
        Review review = Review.builder()
                .item(item)
                .account(account)
                .content("test")
                .title("test")
                .password("test")
                .rating(BigDecimal.valueOf(5))
                .build();
        return reviewRepository.save(review);
    }

    public Item createItem() {
        Item item = Item.builder()
                .itemNm("테스트 상품")
                .price(10000)
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