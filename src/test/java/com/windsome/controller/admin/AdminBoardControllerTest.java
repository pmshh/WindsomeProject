package com.windsome.controller.admin;

import com.windsome.WithAccount;
import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.board.notice.NoticeListDTO;
import com.windsome.dto.board.qa.QaListDTO;
import com.windsome.dto.board.review.ReviewListDTO;
import com.windsome.service.board.BoardService;
import com.windsome.service.cart.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

//@WebMvcTest(AdminBoardController.class)
@SpringBootTest
@AutoConfigureMockMvc
@MockBean(JpaMetamodelMappingContext.class)
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AdminBoardControllerTest {

    @MockBean BoardService boardService;
    @MockBean CartService cartService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("공지사항 조회 테스트")
    @WithAccount("admin1234")
    void getNoticeListTest() throws Exception {
        // Given
        Page<NoticeListDTO> noticeList = new PageImpl<>(Collections.emptyList());
        given(boardService.getNoticeList(any(SearchDTO.class), any())).willReturn(noticeList);

        // Perform & Verify
        mockMvc.perform(get("/admin/board/notices")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 성공 테스트")
    @WithAccount("admin1234")
    void deleteNoticeSuccessTest() throws Exception {
        // Mocking - 게시글 삭제가 성공하는 경우
        doNothing().when(boardService).deletePosts(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/notices")
                        .param("noticeIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("게시글이 삭제되었습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    @WithAccount("admin1234")
    void deleteNoticeFailureNotFoundTest() throws Exception {
        // Mocking - 존재하지 않는 게시글 삭제 시 EntityNotFoundException이 발생하는 경우
        doThrow(EntityNotFoundException.class).when(boardService).deletePosts(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/notices")
                        .param("noticeIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("존재하지 않는 게시글입니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 성공")
    @WithAccount("admin1234")
    void updateNoticeSuccessTest() throws Exception {
        // Given
        given(boardService.checkNoticeYN(anyLong(), anyBoolean())).willReturn(false);
        doNothing().when(boardService).updateNoticeYN(anyLong(), anyBoolean());

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notices/1")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 이미 공지글로 설정된 경우")
    @WithAccount("admin1234")
    void updateNoticeAlreadyNoticeTest() throws Exception {
        // Given
        given(boardService.checkNoticeYN(anyLong(), anyBoolean())).willReturn(true);

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notices/1")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 공지글로 설정되어있습니다."));
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 존재하지 않는 게시글인 경우")
    @WithAccount("admin1234")
    void updateNoticeNotFoundTest() throws Exception {
        // Given
        doThrow(EntityNotFoundException.class).when(boardService).updateNoticeYN(anyLong(), anyBoolean());

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notices/1")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 게시글입니다."));
    }

    @Test
    @DisplayName("Q&A 조회 테스트")
    @WithAccount("admin1234")
    void getQaListTest() throws Exception {
        // Given
        Page<QaListDTO> qaListDtos = new PageImpl<>(Collections.emptyList());
        given(boardService.getQaList(any(SearchDTO.class), any())).willReturn(qaListDtos);

        // Perform & Verify
        mockMvc.perform(get("/admin/board/qa")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/board/qa-board-management"))
                .andExpect(model().attributeExists("qaList", "searchDTO", "maxPage", "page"));
    }

    @Test
    @DisplayName("Q&A 삭제 성공 테스트")
    @WithAccount("admin1234")
    void deleteQaSuccessTest() throws Exception {
        // Given
        doNothing().when(boardService).deletePosts(any());

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/qa")
                        .param("qaIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("게시글이 삭제되었습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 Q&A 삭제 시도 테스트")
    @WithAccount("admin1234")
    void deleteNonExistingQaTest() throws Exception {
        // Given
        doThrow(EntityNotFoundException.class).when(boardService).deletePosts(any());

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/qa")
                        .param("qaIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 게시글입니다."));
    }

    @Test
    @DisplayName("리뷰 조회 테스트")
    @WithAccount("admin1234")
    void getReviewListTest() throws Exception {
        // Given
        Page<ReviewListDTO> mockReviewPage = new PageImpl<>(Collections.emptyList());
        given(boardService.getReviews(any(SearchDTO.class), any(Pageable.class))).willReturn(mockReviewPage);

        // Perform & Verify
        mockMvc.perform(get("/admin/board/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/board/review-board-management"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attributeExists("searchDTO"))
                .andExpect(model().attributeExists("maxPage"));
    }

    @Test
    @DisplayName("게시글 삭제 테스트 - 존재하는 게시글인 경우")
    @WithAccount("admin1234")
    void deleteReviewExistingTest() throws Exception {
        // Given
        doNothing().when(boardService).deletePosts(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/reviews")
                        .param("reviewIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("게시글이 삭제되었습니다."));
    }

    @Test
    @DisplayName("게시글 삭제 테스트 - 존재하지 않는 게시글인 경우")
    @WithAccount("admin1234")
    void deleteReviewNotFoundTest() throws Exception {
        // Given
        doThrow(EntityNotFoundException.class).when(boardService).deletePosts(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/reviews")
                        .param("reviewIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 게시글입니다."));
    }
}