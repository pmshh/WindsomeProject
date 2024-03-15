package com.windsome.controller;

import com.windsome.WithAccount;
import com.windsome.constant.ProductSellStatus;
import com.windsome.constant.Role;
import com.windsome.controller.board.ReviewController;
import com.windsome.entity.member.Member;
import com.windsome.entity.product.Product;
import com.windsome.entity.product.ProductImage;
import com.windsome.entity.board.Review;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.board.review.ReviewRepository;
import com.windsome.service.board.ReviewService;
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
    @Autowired ProductRepository productRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired ProductImageRepository productImageRepository;

    @AfterEach
    void afterEach() {
        productImageRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
        reviewRepository.deleteAll();
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 게시판 화면 보이는지 테스트")
    @Test
    void reviews() throws Exception {
        mockMvc.perform(get("/board/review"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reviewSearchDto"))
                .andExpect(model().attributeExists("totalCartProductCount"))
                .andExpect(model().attributeExists("maxPage"))
                .andExpect(view().name("board/review/review-list"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 상세 화면 보이는지 테스트")
    @Test
    void reviewDtl() throws Exception {
        Review review = createReview(createProduct(), saveMember());

        mockMvc.perform(get("/board/review/" + review.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("review"))
                .andExpect(view().name("board/review/review-detail"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 화면 보이는지 테스트")
    @Test
    void enrollReviewForm() throws Exception {
        mockMvc.perform(get("/board/review/enroll"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reviewDto"))
                .andExpect(view().name("board/review/review-new-post"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 - 상품 검색 테스트")
    @Test
    void getItemList() throws Exception {
        createProduct();

        mockMvc.perform(get("/board/review/product-selection")
                        .param("searchQuery", "test"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("searchQuery"))
                .andExpect(model().attributeExists("size"))
                .andExpect(model().attributeExists("maxPage"))
                .andExpect(view().name("board/review/product-selection-popup"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 테스트")
    @Test
    void enrollReview() throws Exception {
        Product product = createProduct();

        String requestJson = "{\"productId\":\"" + product.getId() + "\", \"title\": \"test1234\"" +
                ", \"rating\": \"5\", \"content\": \"test1234\", \"password\": \"test1234\"}";

        mockMvc.perform(post("/board/review/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("리뷰가 등록되었습니다."));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 테스트 - 실패 (db에 존재하지 않는 productId 전송)")
    @Test
    void enrollReviewFail() throws Exception {
        Product product = createProduct();

        String requestJson = "{\"productId\":\"" + (product.getId() + 1) + "\", \"title\": \"test1234\"" +
                ", \"rating\": \"5\", \"content\": \"test1234\", \"password\": \"test1234\"}";

        mockMvc.perform(post("/board/review/enroll")
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
        Review review = createReview(createProduct(), saveMember());

        mockMvc.perform(get("/board/review/update/" + review.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("review"))
                .andExpect(view().name("board/review/review-update-post"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정 테스트 - 권한 있는 사용자")
    @Test
    void updateReview() throws Exception {
        Product product = createProduct();
        Review review = createReview(product, memberRepository.findByUserIdentifier("test1234"));

        String requestJson = "{\"reviewId\":\"" + review.getId() + "\", \"title\": \"test1234\"" +
                ", \"content\": \"test1234\",  \"productId\": \"" + product.getId() + "\", \"password\": \"12341234\", \"rating\": \"5\"}";

        mockMvc.perform(patch("/board/review/" + review.getId())
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
        Product product = createProduct();
        Review review = createReview(product, saveMember());

        String requestJson = "{\"reviewId\":\"" + review.getId() + "\", \"title\": \"test1234\"" +
                ", \"content\": \"test1234\",  \"productId\": \"" + product.getId() + "\", \"password\": \"12341234\", \"rating\": \"5\"}";

        mockMvc.perform(patch("/board/review/" + review.getId())
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
        Member member = memberRepository.findByUserIdentifier("test1234");
        Product product = createProduct();
        Review review = createReview(product, member);

        mockMvc.perform(delete("/board/review/" + review.getId())
                        .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                        .param("productId", String.valueOf(product.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("리뷰가 삭제되었습니다."));;
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 삭제 테스트 - 권한 없는 사용자")
    @Test
    void deleteReviewFail() throws Exception {
        Product product = createProduct();
        Review review = createReview(product, saveMember());

        mockMvc.perform(delete("/board/review/" + review.getId())
                        .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                        .param("productId", String.valueOf(product.getId()))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("삭제 권한이 없습니다."));
    }

    public Review createReview(Product product, Member member) {
        Review review = Review.builder()
                .product(product)
                .member(member)
                .content("test")
                .title("test")
                .password("test")
                .rating(BigDecimal.valueOf(5))
                .build();
        return reviewRepository.save(review);
    }

    public Product createProduct() {
        Product product = Product.builder()
                .name("테스트 상품")
                .price(10000)
                .productDetail("테스트 상품 상세 설명")
                .productSellStatus(ProductSellStatus.AVAILABLE)
//                .stockNumber(100)
                .build();
        productRepository.save(product);

        ProductImage productImage = ProductImage.builder()
                .product(product)
                .originalImageName("test")
                .serverImageName("test")
                .imageUrl("test")
                .isRepresentativeImage(true)
                .build();
        productImageRepository.save(productImage);

        return product;
    }

    public Member saveMember() {
        Member member = Member.builder()
                .userIdentifier("test0000")
                .password("test0000")
                .name("test")
                .email("test0000@naver.com")
                .zipcode("test")
                .addr("test")
                .addrDetail("test")
                .role(Role.USER)
                .availablePoints(0)
                .totalUsedPoints(0)
                .totalEarnedPoints(0)
                .build();
        return memberRepository.save(member);
    }
}