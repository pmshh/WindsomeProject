package com.windsome.controller;

import com.windsome.WithAccount;
import com.windsome.constant.ProductSellStatus;
import com.windsome.constant.Role;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Member;
import com.windsome.entity.product.Product;
import com.windsome.entity.product.ProductImage;
import com.windsome.repository.board.BoardRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.service.board.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
public class BoardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired BoardService boardService;
    @Autowired BoardRepository boardRepository;
    @Autowired ProductRepository productRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired ProductImageRepository productImageRepository;

    @Test
    @DisplayName("공지사항 게시판 화면 보이는지 테스트")
    public void testNotices() throws Exception {
        mockMvc.perform(get("/board/notices"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("fixTopNoticeList"))
                .andExpect(model().attributeExists("noticeList"))
                .andExpect(model().attributeExists("searchDTO"))
                .andExpect(model().attributeExists("maxPage"))
                .andExpect(model().attributeExists("page"))
                .andExpect(view().name("board/notice/notice-list"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("공지사항 상세 화면 보이는지 테스트")
    public void noticeDtl() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Notice").title("test").content("test").member(member).build();
        boardRepository.save(board);

        mockMvc.perform(get("/board/notices/{noticeId}", 1L))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("noticeDtlList"))
                .andExpect(model().attributeExists("page"))
                .andExpect(view().name("board/notice/notice-detail"));
    }

    /**
     * Q&A TEST
     */
    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 게시판 화면 보이는지 테스트")
    public void qa() throws Exception {
        mockMvc.perform(get("/board/qa"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("qaList"))
                .andExpect(model().attributeExists("searchDTO"))
                .andExpect(model().attributeExists("maxPage"))
                .andExpect(model().attributeExists("page"))
                .andExpect(view().name("board/qa/qa-list"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 등록 화면 보이는지 테스트")
    public void enrollQaForm() throws Exception {
        mockMvc.perform(get("/board/qa/enroll"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("qaEnrollDto"))
                .andExpect(view().name("board/qa/qa-new-post"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 등록 테스트")
    public void enrollQa() throws Exception {
        mockMvc.perform(post("/board/qa/enroll")
                        .param("title", "test")
                        .param("content", "test")
                        .param("password", "12341234")
                        .param("hasPrivate", "true")
                        .param("originNo","0")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("게시글이 등록되었습니다."));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 비밀글 조회 시, 비밀번호 입력 화면 보이는지 테스트")
    public void secretForm() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Q&A").title("test").content("test").hasPrivate(true).password("12341234").member(member).build();
        boardRepository.save(board);

        mockMvc.perform(get("/board/qa/{qaId}/password-verification", 1L))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("password"))
                .andExpect(view().name("board/qa/qa-secret-post-verification"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 비밀글 조회 시, 비밀번호 검증 테스트")
    public void secret() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Q&A").title("test").content("test").hasPrivate(true).password("12341234").member(member).build();
        boardRepository.save(board);

        mockMvc.perform(post("/board/qa/{qaId}/password-verification", 1L)
                        .param("password", "12341234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 상세 화면 보이는지 테스트")
    public void qaDtl() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Q&A").title("test").content("test").hasPrivate(true).password("12341234").member(member).build();
        boardRepository.save(board);
        Board findBoard = boardRepository.findByMemberId(member.getId());

        mockMvc.perform(get("/board/qa/{qaId}", findBoard.getId())
                        .param("password","12341234"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("qaDtlDtoList"))
                .andExpect(model().attributeExists("commentDtoList"))
                .andExpect(model().attributeExists("password"))
                .andExpect(view().name("board/qa/qa-detail"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 수정 화면 보이는지 테스트")
    public void updateQaForm() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Q&A").title("test").content("test").hasPrivate(true).password("12341234").member(member).build();
        boardRepository.save(board);
        Board findBoard = boardRepository.findByMemberId(member.getId());

        mockMvc.perform(get("/board/qa/update/{qaId}", findBoard.getId())
                        .param("password","12341234"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("qaUpdateDto"))
                .andExpect(view().name("board/qa/qa-update-post"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 수정 테스트")
    public void updateQa() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Q&A").title("test").content("test").hasPrivate(true).password("12341234").member(member).build();
        boardRepository.save(board);
        Board findBoard = boardRepository.findByMemberId(member.getId());

        mockMvc.perform(patch("/board/qa/{qaId}", findBoard.getId())
                        .param("title","test")
                        .param("content","test")
                        .param("hasPrivate","true")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("게시글이 수정되었습니다."));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("Q&A 삭제 테스트")
    public void deleteQa() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Q&A").title("test").content("test").hasPrivate(true).password("12341234").member(member).build();
        boardRepository.save(board);
        Board findBoard = boardRepository.findByMemberId(member.getId());

        mockMvc.perform(delete("/board/qa/{qaId}", findBoard.getId())
                        .param("password","12341234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("게시글이 삭제되었습니다."));
    }

    /**
     * Review TEST
     */
    @WithAccount("test1234")
    @DisplayName("리뷰 게시판 화면 보이는지 테스트")
    @Test
    void reviews() throws Exception {
        mockMvc.perform(get("/board/reviews"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("searchDTO"))
                .andExpect(model().attributeExists("totalCartProductCount"))
                .andExpect(model().attributeExists("maxPage"))
                .andExpect(view().name("board/review/review-list"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 상세 화면 보이는지 테스트")
    @Test
    void reviewDtl() throws Exception {
        Board review = createReview(createProduct(), saveMember());

        mockMvc.perform(get("/board/reviews/" + review.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("review"))
                .andExpect(view().name("board/review/review-detail"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 화면 보이는지 테스트")
    @Test
    void enrollReviewForm() throws Exception {
        mockMvc.perform(get("/board/reviews/enroll"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reviewDto"))
                .andExpect(view().name("board/review/review-new-post"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 등록 - 상품 검색 테스트")
    @Test
    void getItemList() throws Exception {
        createProduct();

        mockMvc.perform(get("/board/reviews/product-selection")
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

        mockMvc.perform(post("/board/reviews/enroll")
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

        mockMvc.perform(post("/board/reviews/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("리뷰 등록 도중 오류가 발생하였습니다."));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정 화면 보이는지 테스트")
    @Test
    void updateReviewForm() throws Exception {
        Board review = createReview(createProduct(), saveMember());

        mockMvc.perform(get("/board/reviews/update/" + review.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("review"))
                .andExpect(view().name("board/review/review-update-post"));
    }

    @WithAccount("test1234")
    @DisplayName("리뷰 수정 테스트 - 권한 있는 사용자")
    @Test
    void updateReview() throws Exception {
        Product product = createProduct();
        Board review = createReview(product, memberRepository.findByUserIdentifier("test1234"));

        String requestJson = "{\"reviewId\":\"" + review.getId() + "\", \"title\": \"test1234\"" +
                ", \"content\": \"test1234\",  \"productId\": \"" + product.getId() + "\", \"password\": \"12341234\", \"rating\": \"5\"}";

        mockMvc.perform(patch("/board/reviews/" + review.getId())
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
        Board review = createReview(product, saveMember());

        String requestJson = "{\"reviewId\":\"" + review.getId() + "\", \"title\": \"test1234\"" +
                ", \"content\": \"test1234\",  \"productId\": \"" + product.getId() + "\", \"password\": \"12341234\", \"rating\": \"5\"}";

        mockMvc.perform(patch("/board/reviews/" + review.getId())
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
        Board review = createReview(product, member);

        mockMvc.perform(delete("/board/reviews/" + review.getId())
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
        Board review = createReview(product, saveMember());

        mockMvc.perform(delete("/board/reviews/" + review.getId())
                        .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                        .param("productId", String.valueOf(product.getId()))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(content().string("삭제 권한이 없습니다."));
    }

    public Board createReview(Product product, Member member) {
        Board review = Board.builder()
                .product(product)
                .member(member)
                .content("test")
                .title("test")
                .password("test")
                .rating(BigDecimal.valueOf(5))
                .build();
        return boardRepository.save(review);
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
                .role(Role.USER)
                .availablePoints(0)
                .totalUsedPoints(0)
                .totalEarnedPoints(0)
                .build();
        return memberRepository.save(member);
    }
}
