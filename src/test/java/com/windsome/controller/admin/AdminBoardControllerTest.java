package com.windsome.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsome.WithAccount;
import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.board.notice.NoticeDtlDTO;
import com.windsome.dto.board.notice.NoticeListDTO;
import com.windsome.dto.board.qa.QaListDTO;
import com.windsome.dto.board.review.ReviewListDTO;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Member;
import com.windsome.repository.board.BoardRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.service.AdminService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@MockBean(JpaMetamodelMappingContext.class)
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AdminBoardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired BoardRepository boardRepository;
    @MockBean AdminService adminService;
    @MockBean BoardService boardService;
    @MockBean CartService cartService;

    @Test
    @DisplayName("공지 전체 조회 테스트")
    @WithAccount("ADMIN")
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
    @WithAccount("ADMIN")
    @DisplayName("공지 등록 화면 보이는지 테스트")
    public void enrollNoticeForm() throws Exception {
        mockMvc.perform(get("/admin/board/notices/enroll"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("boardDTO"))
                .andExpect(view().name("admin/board/notice/notice-new-post"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("공지 등록 테스트")
    public void enrollNoticeWithSuccess() throws Exception {
        mockMvc.perform(post("/admin/board/notices/enroll")
                        .param("title","test")
                        .param("content","test")
                        .param("hasNotice","true")
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/board/notices"))
                .andExpect(flash().attribute("message", "게시글이 등록되었습니다."));;
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("공지 상세 화면 보이는지 테스트")
    public void noticeDtl() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Notice").title("test").content("test").member(member).hasNotice(false).build();
        Board savedBoard = boardRepository.save(board);

        List<NoticeDtlDTO> noticeDtlDTOList = new ArrayList<>();
        NoticeDtlDTO noticeDtlDTO = new NoticeDtlDTO();
        noticeDtlDTO.setTitle("test");
        noticeDtlDTO.setContent("test");
        noticeDtlDTO.setHasNotice(false);
        noticeDtlDTOList.add(noticeDtlDTO);

        when(adminService.getNoticeDtlList(savedBoard.getId())).thenReturn(noticeDtlDTOList);

        mockMvc.perform(get("/admin/board/notices/{noticeId}", savedBoard.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("noticeDtlList"))
                .andExpect(model().attributeExists("page"))
                .andExpect(view().name("admin/board/notice/notice-detail"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("공지 수정 화면 보이는지 테스트")
    public void updateNoticeForm() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Notice").title("test").content("test").member(member).hasNotice(false).build();
        Board savedBoard = boardRepository.save(board);

        when(adminService.getNotice(savedBoard.getId())).thenReturn(board);

        mockMvc.perform(get("/admin/board/notices/update/{noticeId}", savedBoard.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("noticeDetail"))
                .andExpect(model().attributeExists("page"))
                .andExpect(view().name("admin/board/notice/notice-update-post"));
    }

    @Test
    @WithAccount("ADMIN")
    @DisplayName("공지 수정 테스트")
    public void updateNotice() throws Exception {
        Member member = memberRepository.findByUserIdentifier("ADMIN");
        Board board = Board.builder().boardType("Notice").title("test").content("test").member(member).build();
        boardRepository.save(board);
        Board findBoard = boardRepository.findByMemberId(member.getId());

        mockMvc.perform(put("/admin/board/notices/update/{noticeId}", findBoard.getId())
                        .param("title","title 수정")
                        .param("content", "content 수정")
                        .param("hasPrivate", "true")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("게시글이 수정되었습니다."));
    }

    @Test
    @DisplayName("공지 설정 여부 수정 테스트 - 성공")
    @WithAccount("ADMIN")
    void updateNoticeSuccessTest() throws Exception {
        // Given
        given(adminService.checkNoticeYN(anyLong(), anyBoolean())).willReturn(false);
        doNothing().when(adminService).updateNoticeYN(anyLong(), anyBoolean());

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notices/update/1/has-private")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("공지 설정 여부 수정 테스트 - 이미 공지글로 설정된 경우")
    @WithAccount("ADMIN")
    void updateNoticeAlreadyNoticeTest() throws Exception {
        // Given
        given(adminService.checkNoticeYN(anyLong(), anyBoolean())).willReturn(true);

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notices/update/1/has-private")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 공지글로 설정되어있습니다."));
    }

    @Test
    @DisplayName("공지 설정 여부 수정 테스트 - 존재하지 않는 게시글인 경우")
    @WithAccount("ADMIN")
    void updateNoticeNotFoundTest() throws Exception {
        // Given
        doThrow(EntityNotFoundException.class).when(adminService).updateNoticeYN(anyLong(), anyBoolean());

        // Perform & Verify
        mockMvc.perform(patch("/admin/board/notices/update/1/has-private")
                        .param("noticeYn", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 게시글입니다."));
    }

    @Test
    @DisplayName("Q&A 조회 테스트")
    @WithAccount("ADMIN")
    void getQaListTest() throws Exception {
        // Given
        Page<QaListDTO> qaListDtos = new PageImpl<>(Collections.emptyList());
        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchDateType("title");

        given(boardService.getQaList(any(SearchDTO.class), any())).willReturn(qaListDtos);

        // Perform & Verify
        mockMvc.perform(get("/admin/board/qa")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/board/qa/qa-board-management"))
                .andExpect(model().attributeExists("qaList", "searchDTO", "maxPage", "page"));
    }

    @Test
    @DisplayName("리뷰 조회 테스트")
    @WithAccount("ADMIN")
    void getReviewListTest() throws Exception {
        // Given
        Page<ReviewListDTO> mockReviewPage = new PageImpl<>(Collections.emptyList());
        given(boardService.getReviews(any(SearchDTO.class), any(Pageable.class))).willReturn(mockReviewPage);

        // Perform & Verify
        mockMvc.perform(get("/admin/board/reviews"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attributeExists("searchDTO"))
                .andExpect(model().attributeExists("maxPage"))
                .andExpect(view().name("admin/board/review/review-board-management"));
    }

    @Test
    @DisplayName("게시글 삭제 테스트 - 존재하는 게시글인 경우")
    @WithAccount("ADMIN")
    void deleteReviewExistingTest() throws Exception {
        // Given
        Long[] boardIds = {1L, 2L, 3L};
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(boardIds);
        doNothing().when(adminService).deletePosts(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/delete")
                        .content(jsonData)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("게시글이 삭제되었습니다."));
    }

    @Test
    @DisplayName("게시글 삭제 테스트 - 존재하지 않는 게시글인 경우")
    @WithAccount("ADMIN")
    void deleteReviewNotFoundTest() throws Exception {
        // Given
        Long[] boardIds = {1L, 2L, 3L};
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(boardIds);
        doThrow(EntityNotFoundException.class).when(adminService).deletePosts(any(Long[].class));

        // Perform & Verify
        mockMvc.perform(delete("/admin/board/delete")
                        .content(jsonData)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("존재하지 않는 게시글입니다."));
    }
}