package com.windsome.controller.admin;

import com.windsome.dto.board.notice.NoticeListDto;
import com.windsome.dto.board.notice.NoticeSearchDto;
import com.windsome.dto.board.qa.QaListDto;
import com.windsome.dto.board.qa.QaSearchDto;
import com.windsome.dto.board.review.ReviewListDto;
import com.windsome.dto.board.review.ReviewSearchDto;
import com.windsome.service.board.NoticeService;
import com.windsome.service.board.QaService;
import com.windsome.service.board.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(AdminBoardController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@MockBean(JpaMetamodelMappingContext.class)
class AdminBoardControllerTest {

    @MockBean NoticeService noticeService;
    @MockBean QaService qaService;
    @MockBean ReviewService reviewService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("공지사항 조회 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getNoticeListTest() throws Exception {
        // Given
        Page<NoticeListDto> noticeList = new PageImpl<>(Collections.emptyList());
        given(noticeService.getNoticeList(any(NoticeSearchDto.class), any())).willReturn(noticeList);

        // Perform & Verify
        mockMvc.perform(get("/admin/board/notice")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 성공 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteNoticeSuccessTest() throws Exception {
        // Mocking - 게시글 삭제가 성공하는 경우
        doNothing().when(noticeService).deleteNotices(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/notice")
                        .param("noticeIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("게시글이 삭제되었습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteNoticeFailureNotFoundTest() throws Exception {
        // Mocking - 존재하지 않는 게시글 삭제 시 EntityNotFoundException이 발생하는 경우
        doThrow(EntityNotFoundException.class).when(noticeService).deleteNotices(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/notice")
                        .param("noticeIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("존재하지 않는 게시글입니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 성공")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateNoticeSuccessTest() throws Exception {
        // Given
        given(noticeService.checkNoticeYN(anyLong(), anyBoolean())).willReturn(false);
        doNothing().when(noticeService).updateNoticeYN(anyLong(), anyBoolean());

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notice/1")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 이미 공지글로 설정된 경우")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateNoticeAlreadyNoticeTest() throws Exception {
        // Given
        given(noticeService.checkNoticeYN(anyLong(), anyBoolean())).willReturn(true);

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notice/1")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 공지글로 설정되어있습니다."));
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 존재하지 않는 게시글인 경우")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateNoticeNotFoundTest() throws Exception {
        // Given
        doThrow(EntityNotFoundException.class).when(noticeService).updateNoticeYN(anyLong(), anyBoolean());

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notice/1")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 게시글입니다."));
    }

    @Test
    @DisplayName("Q&A 조회 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getQaListTest() throws Exception {
        // Given
        Page<QaListDto> qaListDtos = new PageImpl<>(Collections.emptyList());
        given(qaService.getQaList(any(QaSearchDto.class), any())).willReturn(qaListDtos);

        // Perform & Verify
        mockMvc.perform(get("/admin/board/qa")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/board/qaMng"))
                .andExpect(model().attributeExists("qaList", "qaSearchDto", "maxPage", "page"));
    }

    @Test
    @DisplayName("Q&A 삭제 성공 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteQaSuccessTest() throws Exception {
        // Given
        doNothing().when(qaService).deleteQas(any());

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
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteNonExistingQaTest() throws Exception {
        // Given
        doThrow(EntityNotFoundException.class).when(qaService).deleteQas(any());

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
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getReviewListTest() throws Exception {
        // Given
        Page<ReviewListDto> mockReviewPage = new PageImpl<>(Collections.emptyList());
        given(reviewService.getReviews(any(ReviewSearchDto.class), any(Pageable.class))).willReturn(mockReviewPage);

        // Perform & Verify
        mockMvc.perform(get("/admin/board/review"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/board/reviewMng"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attributeExists("reviewSearchDto"))
                .andExpect(model().attributeExists("maxPage"));
    }

    @Test
    @DisplayName("게시글 삭제 테스트 - 존재하는 게시글인 경우")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteReviewExistingTest() throws Exception {
        // Given
        doNothing().when(reviewService).deleteReviews(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/review")
                        .param("reviewIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("게시글이 삭제되었습니다."));
    }

    @Test
    @DisplayName("게시글 삭제 테스트 - 존재하지 않는 게시글인 경우")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteReviewNotFoundTest() throws Exception {
        // Given
        doThrow(EntityNotFoundException.class).when(reviewService).deleteReviews(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/review")
                        .param("reviewIds", "1", "2", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 게시글입니다."));
    }
}