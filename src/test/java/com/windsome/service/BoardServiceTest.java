package com.windsome.service;

import com.windsome.constant.Role;
import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.board.notice.*;
import com.windsome.dto.board.qa.CommentDTO;
import com.windsome.dto.board.qa.QaEnrollDTO;
import com.windsome.dto.board.qa.QaListDTO;
import com.windsome.dto.board.qa.QaUpdateDTO;
import com.windsome.dto.board.review.*;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Member;
import com.windsome.entity.product.Product;
import com.windsome.entity.product.ProductImage;
import com.windsome.repository.board.BoardRepository;
import com.windsome.service.board.BoardService;
import com.windsome.service.board.CommentService;
import com.windsome.service.member.MemberService;
import com.windsome.service.product.ProductImageService;
import com.windsome.service.product.ProductService;
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

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.windsome.TestUtil.createMember;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class BoardServiceTest {

    @Mock private BoardRepository boardRepository;
    @Mock private MemberService memberService;
    @Mock private ProductService productService;
    @Mock private ProductImageService productImageService;
    @Mock private CommentService commentService;

    @InjectMocks private BoardService boardService;

    /**
     * Notice TEST
     */
    @Test
    @DisplayName("일반 공지사항 조회")
    public void testGetNoticeList() {
        // given
        SearchDTO searchDTO = new SearchDTO();
        Pageable pageable = Pageable.ofSize(10).withPage(0);

        List<NoticeListDTO> fakeNoticeList = Collections.singletonList(new NoticeListDTO());
        Page<NoticeListDTO> fakePage = new PageImpl<>(fakeNoticeList, pageable, fakeNoticeList.size());
        when(boardRepository.getNoticeList(searchDTO, pageable)).thenReturn(fakePage);

        // when
        Page<NoticeListDTO> resultPage = boardService.getNoticeList(searchDTO, pageable);

        // then
        verify(boardRepository, times(1)).getNoticeList(searchDTO, pageable);
        assertEquals(fakePage, resultPage);
        assertEquals(resultPage.getSize(), 10);
    }

    @Test
    @DisplayName("상단 고정 공지사항 조회")
    public void testGetFixTopNoticeList() {
        // given
        List<Board> expectedNotices = Arrays.asList(new Board(), new Board());

        when(boardRepository.findAllByHasNoticeOrderByRegTimeDesc(true)).thenReturn(expectedNotices);

        // when
        List<Board> actualNotices = boardService.getFixTopNoticeList();

        // then
        verify(boardRepository, times(1)).findAllByHasNoticeOrderByRegTimeDesc(true);
        assertEquals(expectedNotices, actualNotices);
    }

    @Test
    @DisplayName("공지사항 등록")
    public void testEnrollNotice() {
        // given
        NoticeDTO noticeDto = new NoticeDTO();
        Member member = new Member();

        Board notice = new Board();
        notice.setId(1L);
        notice.setMember(member);

        when(boardRepository.save(any())).thenReturn(notice);

        // when
        Long savedNoticeId = boardService.enrollNotice(noticeDto, member);

        // then
        verify(boardRepository, times(1)).save(any());
        assertEquals(1L, savedNoticeId);
    }

    @Test
    @DisplayName("공지사항 상세 화면 조회")
    public void testGetNoticeDtlList() {
        // given
        Long noticeId = 1L;

        Member member1 = new Member();
        member1.setId(1L);
        member1.setName("홍길동");

        Member member2 = new Member();
        member2.setId(2L);
        member2.setName("박길동");

        NoticeDtlDtoInterface noticeDtoInterface1 = createMockNoticeDtlDto(1L, "제목1", "내용1", true, 1L, LocalDateTime.now());
        NoticeDtlDtoInterface noticeDtoInterface2 = createMockNoticeDtlDto(2L, "제목2", "내용2", false, 2L, LocalDateTime.now());
        List<NoticeDtlDtoInterface> noticeDtlDtoInterfaces = Arrays.asList(noticeDtoInterface1, noticeDtoInterface2);

        when(boardRepository.getNoticeDtl(anyLong())).thenReturn(noticeDtlDtoInterfaces);
        when(memberService.getMemberByMemberId(1L)).thenReturn(member1);
        when(memberService.getMemberByMemberId(2L)).thenReturn(member2);

        // when
        List<NoticeDtlDTO> result = boardService.getNoticeDtlList(noticeId);

        // then
        verify(boardRepository, times(1)).getNoticeDtl(noticeId);
        verify(memberService, times(2)).getMemberByMemberId(anyLong());
        assertEquals(2, result.size());
        assertEquals("제목1", result.get(0).getTitle());
        assertEquals("내용1", result.get(0).getContent());
        assertEquals("제목2", result.get(1).getTitle());
        assertEquals("내용2", result.get(1).getContent());
    }

    @Test
    @DisplayName("공지사항 수정 - 성공")
    public void testUpdateNotice_Success() {
        // given
        Long noticeId = 1L;
        NoticeUpdateDTO noticeUpdateDto = new NoticeUpdateDTO();
        noticeUpdateDto.setTitle("수정된 제목");
        noticeUpdateDto.setContent("수정된 내용");

        Board existingNotice = new Board();
        existingNotice.setId(noticeId);
        existingNotice.setTitle("기존 제목");
        existingNotice.setContent("기존 내용");
        existingNotice.setRegTime(LocalDateTime.now());

        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(existingNotice));

        // when
        boardService.updateNotice(noticeId, noticeUpdateDto);

        // then
        verify(boardRepository, times(1)).findById(noticeId);
        verify(boardRepository, times(1)).save(existingNotice);
        assertEquals("수정된 제목", existingNotice.getTitle());
        assertEquals("수정된 내용", existingNotice.getContent());
    }

    @Test
    @DisplayName("공지사항 수정 - 공지사항을 찾을 수 없는 경우")
    public void testUpdateNotice_NotFound() {
        // given
        Long noticeId = 1L;
        NoticeUpdateDTO noticeUpdateDto = new NoticeUpdateDTO();

        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> boardService.updateNotice(noticeId, noticeUpdateDto));
        verify(boardRepository, times(1)).findById(noticeId);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("공지사항 조회 - 성공")
    public void testGetNotice_Success() {
        // given
        Long noticeId = 1L;
        Board expectedNotice = new Board();
        expectedNotice.setId(noticeId);
        expectedNotice.setTitle("제목");
        expectedNotice.setContent("내용");

        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(expectedNotice));

        // when
        Board result = boardService.getNotice(noticeId);

        // then
        verify(boardRepository, times(1)).findById(noticeId);
        assertEquals(expectedNotice, result);
    }

    @Test
    @DisplayName("공지사항 조회 - 공지사항을 찾을 수 없는 경우")
    public void testGetNotice_NotFound() {
        // given
        Long noticeId = 1L;

        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> boardService.getNotice(noticeId));
        verify(boardRepository, times(1)).findById(noticeId);
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    public void testDeleteNotice_Success() {
        // given
        Long noticeId = 1L;
        Board mockNotice = new Board();
        mockNotice.setId(noticeId);

        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(mockNotice));

        // when
        boardService.deletePost(noticeId);

        // then
        verify(boardRepository, times(1)).findById(noticeId);
        verify(boardRepository, times(1)).deleteById(noticeId);
    }

    @Test
    @DisplayName("게시글 삭제 - 공지사항을 찾을 수 없는 경우")
    public void testDeleteNotice_NotFound() {
        // given
        Long noticeId = 1L;

        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> boardService.deletePost(noticeId));
        verify(boardRepository, times(1)).findById(noticeId);
        verify(boardRepository, times(0)).deleteById(noticeId);
    }

    @Test
    @DisplayName("관리자 권한 - 관리자일 때")
    public void testIsAdmin_Admin() {
        // given
        Member mockMember = mock(Member.class);
        when(mockMember.getRole()).thenReturn(Role.ADMIN);

        // when
        boolean result = boardService.isAdmin(mockMember);

        // then
        assertTrue(result);
        verify(mockMember).getRole();
    }

    @Test
    @DisplayName("관리자 권한 - 관리자가 아닐 때")
    public void testIsAdmin_NotAdmin() {
        // given
        Member mockMember = mock(Member.class);
        when(mockMember.getRole()).thenReturn(Role.USER);

        // when
        boolean result = boardService.isAdmin(mockMember);

        // then
        assertFalse(result);
        verify(mockMember).getRole();
    }

    @Test
    @DisplayName("게시글 여러건 삭제 - 성공")
    public void testDeleteNotices_Success() {
        // given
        Long[] noticeIds = {1L, 2L, 3L};
        Board notice1 = new Board();
        Board notice2 = new Board();
        Board notice3 = new Board();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(notice1));
        when(boardRepository.findById(2L)).thenReturn(Optional.of(notice2));
        when(boardRepository.findById(3L)).thenReturn(Optional.of(notice3));

        // when
        boardService.deletePosts(noticeIds);

        // then
        verify(boardRepository, times(3)).findById(any());
        verify(boardRepository, times(3)).delete(any());
    }

    @Test
    @DisplayName("게시글 여러건 삭제 - 존재하지 않는 게시글")
    public void testDeleteNotices_EntityNotFoundException() {
        // given
        Long[] noticeIds = {1L, 2L, 3L};
        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> boardService.deletePosts(noticeIds));
    }

    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 가능")
    public void testCheckNoticeYN_NoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;

        Board notice = new Board();
        notice.setHasNotice(false);

        when(boardRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        // when
        boolean result = boardService.checkNoticeYN(noticeId, noticeYN);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 불가능(이미 공지글로 설정된 경우)")
    public void testCheckNoticeYN_NotNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;

        Board notice = new Board();
        notice.setHasNotice(true);

        when(boardRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        // when
        boolean result = boardService.checkNoticeYN(noticeId, noticeYN);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 존재하지 않는 공지글")
    public void testCheckNoticeYN_EntityNotFoundException() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;
        when(boardRepository.findById(noticeId)).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> boardService.checkNoticeYN(noticeId, noticeYN));
    }

    @Test
    @DisplayName("공지글 설정 수정 - 공지글로 변경")
    public void testUpdateNoticeYN_SetNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = true;

        Board notice = new Board();
        notice.setHasNotice(false);

        when(boardRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        // when
        boardService.updateNoticeYN(noticeId, noticeYn);

        // then
        assertTrue(notice.isHasNotice());
        verify(boardRepository).save(notice);
    }

    @Test
    @DisplayName("공지글 설정 수정 - 공지글 해제")
    public void testUpdateNoticeYN_UnsetNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = false;

        Board notice = new Board();
        notice.setHasNotice(true);

        when(boardRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        // when
        boardService.updateNoticeYN(noticeId, noticeYn);

        // then
        assertFalse(notice.isHasNotice());
        verify(boardRepository).save(notice);
    }

    @Test
    @DisplayName("공지글 설정 수정 - 존재하지 않는 공지글")
    public void testUpdateNoticeYN_EntityNotFoundException() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = true;

        when(boardRepository.findById(noticeId)).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> boardService.updateNoticeYN(noticeId, noticeYn));
        verify(boardRepository, never()).save(any());
    }

    /**
     * Q&A TEST
     */
    @Test
    @DisplayName("게시글 조회 테스트")
    public void getQaListTest() {
        // Given
        SearchDTO SearchDTO = new SearchDTO();
        Pageable pageable = Pageable.unpaged();

        QaListDTO qaListDto = new QaListDTO();
        Page<QaListDTO> expectedPage = new PageImpl<>(Collections.singletonList(qaListDto));

        when(boardRepository.getQaList(SearchDTO, pageable)).thenReturn(expectedPage);

        // When
        Page<QaListDTO> resultPage = boardService.getQaList(SearchDTO, pageable);

        // Then
        assertEquals(expectedPage, resultPage);
        verify(boardRepository, times(1)).getQaList(SearchDTO, pageable);
        verifyNoMoreInteractions(boardRepository);
    }

    @Test
    @DisplayName("게시글 등록 테스트 - originNo가 0인 경우")
    public void enrollQa_OriginNoIsZero() {
        // Given
        QaEnrollDTO qaEnrollDto = createQaEnrollDto();
        Member member = createMember(1L);

        // When
        boardService.enrollQa(qaEnrollDto, member);

        // Then
        verify(boardRepository, times(2)).save(any(Board.class)); // save 메서드가 호출되었는지 검증
    }

    @Test
    @DisplayName("게시글 등록 테스트 - originNo가 0이 아닌 경우")
    public void enrollQa_OriginNoIsNotZero() {
        // Given
        Member member = createMember(1L);
        QaEnrollDTO qaEnrollDto = createQaEnrollDto();
        qaEnrollDto.setOriginNo(1L);

        Board originQa = new Board(); // 원글 QA
        originQa.setId(1L);
        originQa.setOriginNo(1L);
        originQa.setGroupOrder(0);
        originQa.setGroupLayer(0);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(originQa)); // findById 메서드가 호출될 때 가짜 데이터 반환하도록 설정

        // When
        boardService.enrollQa(qaEnrollDto, member);

        // Then
        verify(boardRepository, times(1)).save(any(Board.class)); // save 메서드가 호출되었는지 검증
        verify(boardRepository, times(1)).findByOriginNoAndGroupOrderGreaterThan(originQa.getOriginNo(), originQa.getGroupOrder()); // findByOriginNoAndGroupOrdGreaterThan 메서드가 호출되었는지 검증
        verify(boardRepository, times(1)).save(argThat(qa -> qa.getGroupOrder() == 1));
        verify(boardRepository, times(1)).save(argThat(qa -> qa.getGroupLayer() == 1));
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 - 관리자일시 true 반환하는지 확인")
    public void validatePost_AdminRole_ReturnsTrue() {
        // Given
        Member member = createMember(1L);
        member.setRole(Role.ADMIN); // 관리자 권한 설정

        // When
        boolean result = boardService.validatePost(member, 1L, "password");

        // Then
        assertFalse(result); // 관리자 권한인 경우 항상 false 반환
        verify(boardRepository, never()).findById(anyLong()); // 관리자 권한인 경우 findById 메서드가 호출되지 않았는지 검증
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 - 잘못된 비밀번호 입력 시 true 반환하는지 확인")
    public void validatePost_WrongPassword_ReturnsTrue() {
        // Given
        Member member1 = createMember(1L);
        member1.setRole(Role.USER); // 일반 사용자 권한 설정
        member1.setUserIdentifier("test1234");

        Member member2 = createMember(2L);
        member2.setRole(Role.USER);
        member2.setUserIdentifier("test5678");

        Board qa = new Board();
        qa.setId(1L);
        qa.setOriginNo(1L);
        qa.setMember(member1);
        qa.setPassword("correctPassword"); // 올바른 비밀번호 설정
        when(boardRepository.findById(1L)).thenReturn(java.util.Optional.of(qa));

        // When
        boolean result = boardService.validatePost(member2, 1L, "wrongPassword");

        // Then
        assertTrue(result); // 잘못된 비밀번호일 경우 true 반환
        verify(boardRepository, times(3)).findById(1L); // findById 메서드가 2번 호출되었는지 검증
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 - 올바른 비밀번호 입력 시 false 반환하는지 확인")
    public void validatePost_CorrectPassword_ReturnsFalse() {
        // Given
        Member member1 = createMember(1L);
        member1.setRole(Role.USER); // 일반 사용자 권한 설정
        member1.setUserIdentifier("test1234");

        Member member2 = createMember(2L);
        member2.setRole(Role.USER);
        member2.setUserIdentifier("test5678");

        Board qa = new Board();
        qa.setId(1L);
        qa.setOriginNo(1L);
        qa.setMember(member1);
        qa.setPassword("correctPassword"); // 올바른 비밀번호 설정
        when(boardRepository.findById(1L)).thenReturn(java.util.Optional.of(qa));

        // When
        boolean result = boardService.validatePost(member2, 1L, "correctPassword");

        // Then
        assertFalse(result); // 올바른 비밀번호일 경우 false 반환
        verify(boardRepository, times(3)).findById(1L); // findById 메서드가 1번 호출되었는지 검증
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 - 비밀번호 null 일 시 true 반환하는지 확인")
    public void validatePost_NullPassword_ReturnsTrue() {
        // Given
        Member member1 = createMember(1L);
        member1.setRole(Role.USER); // 일반 사용자 권한 설정
        member1.setUserIdentifier("test1234");

        Member member2 = createMember(2L);
        member2.setRole(Role.USER);
        member2.setUserIdentifier("test5678");

        Board qa = new Board();
        qa.setId(1L);
        qa.setOriginNo(1L);
        qa.setMember(member1);
        qa.setPassword("correctPassword"); // 올바른 비밀번호 설정
        when(boardRepository.findById(1L)).thenReturn(java.util.Optional.of(qa));


        // When
        boolean result = boardService.validatePost(member2, 1L, null); // 비밀번호가 null인 경우

        // Then
        assertTrue(result); // 비밀번호가 null일 경우 true 반환
        verify(boardRepository, times(2)).findById(anyLong()); // findById 메서드가 호출되지 않았는지 검증
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    public void deleteQa_DeletesQaSuccessfully() {
        // Given
        Long qaId = 1L;
        Board qa = new Board();
        qa.setId(qaId);

        // findById 메소드에 대한 스텁 설정
        when(boardRepository.findById(qaId)).thenReturn(java.util.Optional.of(qa));

        // When
        boardService.deletePost(qaId);

        // Then
        verify(boardRepository, times(1)).findById(qaId); // findById 메소드가 호출됐는지 검증
        verify(boardRepository, times(1)).delete(qa); // delete 메소드가 호출됐는지 검증
    }

    @Test
    @DisplayName("선택/전체 게시글 삭제 테스트")
    public void deleteQas_DeletesAllQasSuccessfully() {
        // Given
        Long[] qaIds = {1L, 2L, 3L}; // 테스트용 qaIds 배열

        // findById 메소드에 대한 스텁 설정
        Board qa = new Board(); // 실제 Q&A 객체 생성
        for (Long qaId : qaIds) {
            when(boardRepository.findById(qaId)).thenReturn(java.util.Optional.of(qa));
        }

        // When
        boardService.deletePosts(qaIds);

        // Then
        for (Long qaId : qaIds) {
            verify(boardRepository, times(1)).findById(qaId); // findById 메서드가 호출됐는지 검증
        }

        verify(boardRepository, times(3)).delete(qa); // delete 메서드가 호출됐는지 검증
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 테스트 - 올바른 비밀번호 입력")
    void validatePostPassword_ValidPassword_ReturnsTrue() {
        // Given
        Long qaId = 1L;
        String password = "correctPassword";
        Board qa = new Board();
        qa.setPassword(password);
        when(boardRepository.findById(qaId)).thenReturn(Optional.of(qa));

        // When
        boolean result = boardService.validatePostPassword(qaId, password);

        // Then
        assertTrue(result);
        verify(boardRepository, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 테스트 - 잘못된 비밀번호 입력")
    void validatePostPassword_InvalidPassword_ReturnsFalse() {
        // Given
        Long qaId = 1L;
        String password = "incorrectPassword";
        Board qa = new Board();
        qa.setPassword("correctPassword"); // 실제 비밀번호와 다른 비밀번호 설정
        when(boardRepository.findById(qaId)).thenReturn(Optional.of(qa));

        // When
        boolean result = boardService.validatePostPassword(qaId, password);

        // Then
        assertFalse(result);
        verify(boardRepository, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 테스트 - EntityNotFoundException 발생하는지 확인")
    void validatePostPassword_QaNotFound_ThrowsException() {
        // Given
        Long qaId = 1L;
        when(boardRepository.findById(qaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> boardService.validatePostPassword(qaId, "password"));
        verify(boardRepository, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 성공")
    void updateQa_QaExists_UpdateSuccessful() {
        // Given
        QaUpdateDTO qaUpdateDto = new QaUpdateDTO();
        qaUpdateDto.setQaId(1L);
        qaUpdateDto.setTitle("Updated Title");
        qaUpdateDto.setContent("Updated Content");

        Board existingQa = new Board();
        existingQa.setId(1L);
        existingQa.setTitle("Old Title");
        existingQa.setContent("Old Content");

        when(boardRepository.findById(qaUpdateDto.getQaId())).thenReturn(Optional.of(existingQa));

        // When
        boardService.updateQa(qaUpdateDto);

        // Then
        verify(boardRepository, times(1)).findById(qaUpdateDto.getQaId());
        verify(boardRepository, times(1)).save(any(Board.class));

        assertEquals("Updated Title", existingQa.getTitle());
        assertEquals("Updated Content", existingQa.getContent());
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 실패(존재하지 않는 게시글 접근)")
    void updateQa_QaNotFound_ThrowsException() {
        // Given
        QaUpdateDTO qaUpdateDto = new QaUpdateDTO();
        qaUpdateDto.setQaId(1L);
        qaUpdateDto.setTitle("Updated Title");
        qaUpdateDto.setContent("Updated Content");

        when(boardRepository.findById(qaUpdateDto.getQaId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> boardService.updateQa(qaUpdateDto));
        verify(boardRepository, times(1)).findById(qaUpdateDto.getQaId());
        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    @DisplayName("단일 게시글 상세 조회 테스트 - 성공")
    void getQaForUpdate_QaExists_ReturnsDto() {
        // Given
        Long qaId = 1L;
        Board qa = new Board();
        qa.setId(qaId);
        qa.setTitle("Test Title");
        qa.setContent("Test Content");

        when(boardRepository.findById(qaId)).thenReturn(Optional.of(qa));

        // When
        QaUpdateDTO resultDto = boardService.getQaForUpdate(qaId);

        // Then
        assertNotNull(resultDto);
        assertEquals(qaId, resultDto.getQaId());
        assertEquals("Test Title", resultDto.getTitle());
        assertEquals("Test Content", resultDto.getContent());

        verify(boardRepository, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("단일 게시글 상세 조회 테스트 - EntityNotFoundException 발생하는지 확인")
    void getQaForUpdate_QaNotFound_ThrowsException() {
        // Given
        Long qaId = 1L;
        when(boardRepository.findById(qaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> boardService.getQaForUpdate(qaId));

        verify(boardRepository, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("게시글 댓글 조회 테스트")
    void getCommentList_ReturnsListOfCommentDto() {
        // Given
        Long qaId = 1L;
        List<CommentDTO> expectedComments = new ArrayList<>();
        expectedComments.add(new CommentDTO("Comment 1"));
        expectedComments.add(new CommentDTO("Comment 2"));

        when(commentService.getCommentDtoList(qaId)).thenReturn(expectedComments);

        // When
        List<CommentDTO> actualComments = boardService.getCommentList(qaId);

        // Then
        assertEquals(expectedComments.size(), actualComments.size());
        for (int i = 0; i < expectedComments.size(); i++) {
            assertEquals(expectedComments.get(i).getContent(), actualComments.get(i).getContent());
        }

        verify(commentService, times(1)).getCommentDtoList(qaId);
    }

    /**
     * Review TEST
     */
    @Test
    @DisplayName("리뷰 등록 - 성공")
    public void testEnrollReview_Success() {
        // given
        ReviewEnrollDTO reviewEnrollDto = new ReviewEnrollDTO();
        reviewEnrollDto.setProductId(1L);
        Member member = new Member();
        Product product = new Product();
        when(productService.getProductByProductId(1L)).thenReturn(product);

        // when
        boardService.enrollReview(reviewEnrollDto, member);

        // then
        verify(productService, times(1)).getProductByProductId(1L);
        verify(boardRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("리뷰 등록 - 실패 (상품이 존재하지 않는 경우)")
    public void testEnrollReview_EntityExistsException() {
        // given
        ReviewEnrollDTO reviewEnrollDto = new ReviewEnrollDTO();
        reviewEnrollDto.setProductId(2L);
        Member member = new Member();

        when(productService.getProductByProductId(reviewEnrollDto.getProductId())).thenThrow(new EntityNotFoundException());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.enrollReview(reviewEnrollDto, member);});

        // verify
        verify(productService, times(1)).getProductByProductId(2L);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 리스트 조회")
    public void testGetProductList() {
        // given
        ProductSearchDTO searchDto = new ProductSearchDTO();
        Pageable pageable = Pageable.ofSize(10).withPage(0);

        List<ProductListDTO> productList = new ArrayList<>();
        productList.add(new ProductListDTO());
        productList.add(new ProductListDTO());
        when(productService.getReviewPageItemList(any(), any())).thenReturn(productList);
        when(productService.getReviewPageItemListCount(any())).thenReturn(2L);

        // when
        PageImpl<ProductListDTO> resultPage = boardService.getProductList(searchDto, pageable);

        // then
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(2, resultPage.getContent().size());
        assertEquals(10, resultPage.getSize());
    }

    @Test
    @DisplayName("리뷰 작성 화면 접근 - 성공")
    public void testGetProduct_Success() {
        // given
        Long productId = 1L;
        Product product = new Product();
        product.setName("상품1");
        when(productService.getProductByProductId(productId)).thenReturn(product);

        ProductImage productImage = new ProductImage();
        productImage.setImageUrl("이미지URL");
        when(productImageService.getRepresentativeImageUrl(productId, true)).thenReturn(productImage.getImageUrl());

        // when
        ProductDTO result = boardService.getProduct(productId);

        // then
        assertEquals("상품1", result.getProductName());
        assertEquals("이미지URL", result.getImageUrl());
    }

    @Test
    @DisplayName("리뷰 작성 화면 접근 - 실패 (상품이 존재하지 않는 경우)")
    public void testGetProduct_EntityNotFoundException() {
        // given
        Long itemId = 1L;
        when(productService.getProductByProductId(anyLong())).thenThrow(new EntityNotFoundException());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.getProduct(itemId);});
    }

    @Test
    @DisplayName("리뷰 조회 - 성공")
    public void testGetReviewDtl_Success() {
        // given
        Long reviewId = 1L;
        Board review = new Board();
        review.setId(reviewId);
        Product product = new Product();
        product.setId(2L);
        review.setProduct(product);
        when(boardRepository.findById(reviewId)).thenReturn(Optional.of(review));

        ProductImage productImage = new ProductImage();
        productImage.setImageUrl("이미지URL");
        when(productImageService.getRepresentativeImageUrl(2L, true)).thenReturn(productImage.getImageUrl());

        // when
        ReviewDtlPageReviewDTO result = boardService.getReviewDtl(reviewId);

        // then
        assertEquals(reviewId, result.getReviewId());
        assertEquals("이미지URL", result.getImageUrl());
    }

    @Test
    @DisplayName("리뷰 조회 - 실패 (리뷰가 존재하지 않는 경우)")
    public void testGetReviewDtl_EntityNotFoundException() {
        // given
        Long reviewId = 1L;
        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.getReviewDtl(reviewId);});
    }

    @Test
    @DisplayName("리뷰 수정 - 성공")
    public void testUpdateReview_Success() {
        // given
        ReviewUpdateDTO reviewUpdateDto = new ReviewUpdateDTO();
        reviewUpdateDto.setReviewId(1L);
        reviewUpdateDto.setTitle("수정된 제목");
        Board existingReview = new Board();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(existingReview));

        // when
        boardService.updateReview(reviewUpdateDto);

        // then
        assertEquals(existingReview.getTitle(), "수정된 제목");
        verify(boardRepository, times(1)).findById(1L);
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    @DisplayName("리뷰 수정 - 실패 (리뷰가 존재하지 않는 경우)")
    public void testUpdateReview_EntityNotFoundException() {
        // given
        ReviewUpdateDTO reviewUpdateDto = new ReviewUpdateDTO();
        reviewUpdateDto.setReviewId(1L);
        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.updateReview(reviewUpdateDto);});

        // verify
        verify(boardRepository, times(1)).findById(1L);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("리뷰 삭제 - 성공")
    public void testDeleteReview_Success() {
        // given
        Long reviewId = 1L;

        Board review = new Board();
        review.setId(1L);

        when(boardRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        boardService.deletePost(reviewId);

        // then
        verify(boardRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("리뷰 삭제 - 실패")
    public void testDeleteReview_Fail() {
        // given
        Long reviewId = 1L;

        when(boardRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> {boardService.deletePost(reviewId);});
    }

    @Test
    @DisplayName("리뷰 소유권 검증 - 소유자일 경우")
    public void testValidateReviewOwnership_Owner() {
        // given
        Long reviewId = 1L;

        Member member = new Member();
        member.setUserIdentifier("owner");

        Board review = new Board();
        Member reviewOwner = new Member();
        reviewOwner.setUserIdentifier("owner");
        review.setMember(reviewOwner);

        when(boardRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        boolean result = boardService.validateReviewOwnership(reviewId, member);

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

        Board review = new Board();
        Member reviewOwner = new Member();
        reviewOwner.setUserIdentifier("owner");
        review.setMember(reviewOwner);

        when(boardRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        boolean result = boardService.validateReviewOwnership(reviewId, member);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("리뷰 소유권 검증 - 리뷰가 존재하지 않는 경우")
    public void testValidateReviewOwnership_ReviewNotFound() {
        // given
        Long reviewId = 1L;
        Member member = new Member();
        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.validateReviewOwnership(reviewId, member);});
    }

    @Test
    @DisplayName("리뷰 조회")
    public void testGetReviews() {
        // given
        SearchDTO searchDTO = new SearchDTO();
        Pageable pageable = Pageable.ofSize(10).withPage(0);

        List<ReviewListDTO> reviewList = new ArrayList<>();
        reviewList.add(new ReviewListDTO());
        reviewList.add(new ReviewListDTO());

        Page<ReviewListDTO> expectedPage = new PageImpl<>(reviewList, pageable, 2);
        when(boardRepository.getReviews(any(), any())).thenReturn(expectedPage);

        // when
        Page<ReviewListDTO> resultPage = boardService.getReviews(searchDTO, pageable);

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

        List<Board> reviewList = new ArrayList<>();
        Board review1 = new Board();
        review1.setId(1L);
        review1.setProduct(product);
        review1.setRegTime(LocalDateTime.now());
        review1.setMember(new Member());
        reviewList.add(review1);

        when(boardRepository.findByProductIdOrderByIdDesc(productId, pageable)).thenReturn(reviewList);
        when(productService.getProductByProductId(productId)).thenReturn(product);
        when(boardRepository.countByProductId(productId)).thenReturn(1L);

        // when
        Page<ProductReviewDTO> resultPage = boardService.getProductReviewList(productId, pageable);

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

        List<Board> reviewList = new ArrayList<>();
        Board review = new Board();
        review.setProduct(product);
        reviewList.add(review);

        when(boardRepository.findByProductIdOrderByIdDesc(productId, pageable)).thenReturn(reviewList);
        when(productService.getProductByProductId(productId)).thenThrow(new EntityNotFoundException());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.getProductReviewList(productId, pageable);});
    }

    @Test
    @DisplayName("평균 평점 설정 - 성공")
    public void testSetRatingAvg_Success() {
        // given
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);

        when(productService.getProductByProductId(productId)).thenReturn(product);
        when(boardRepository.getRatingAvg(productId)).thenReturn(BigDecimal.valueOf(5));

        // when
        boardService.setRatingAvg(productId);

        // then
        verify(productService, times(1)).getProductByProductId(productId);
        verify(boardRepository, times(1)).getRatingAvg(productId);
        assertEquals(BigDecimal.valueOf(5), product.getAverageRating());
        verify(productService, times(1)).saveProduct(product);
    }

    @Test
    @DisplayName("평균 평점 설정 - 실패 (상품이 존재하지 않는 경우)")
    public void testSetRatingAvg_EntityNotFoundException() {
        // given
        Long productId = 1L;
        when(productService.getProductByProductId(anyLong())).thenThrow(new EntityNotFoundException());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.setRatingAvg(productId);});

        // verify
        verify(productService, times(1)).getProductByProductId(productId);
        verify(boardRepository, never()).getRatingAvg(productId);
    }

    @Test
    @DisplayName("리뷰 존재 여부 확인 - 리뷰가 존재하는 경우")
    public void testCheckExistingReview_Exists() {
        // given
        Long productId = 1L;
        Long memberId = 1L;
        when(boardRepository.existsByProductIdAndMemberId(productId, memberId)).thenReturn(true);

        // when
        boolean exists = boardService.checkExistingReview(productId, memberId);

        // then
        assertTrue(exists);
    }

    @Test
    @DisplayName("리뷰 존재 여부 확인 - 리뷰가 존재하지 않는 경우")
    public void testCheckExistingReview_NotExists() {
        // given
        Long productId = 1L;
        Long memberId = 1L;
        when(boardRepository.existsByProductIdAndMemberId(productId, memberId)).thenReturn(false);

        // when
        boolean exists = boardService.checkExistingReview(productId, memberId);

        // then
        assertFalse(exists);
    }

    @Test
    @DisplayName("조회수 증가 및 쿠키 생성 - 쿠키가 없는 경우")
    public void testValidateHitsCount_NoCookie() {
        // given
        ReviewDtlPageReviewDTO reviewDto = new ReviewDtlPageReviewDTO();
        reviewDto.setReviewId(1L);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        HttpServletResponse response = mock(HttpServletResponse.class);

        Board findReview = new Board();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(findReview));

        // when
        boardService.validateHitsCount(reviewDto, request, response);

        // then
        assertEquals(findReview.getHits(), 1);
        verify(boardRepository, times(1)).findById(1L);
        verify(boardRepository, times(1)).save(findReview);
        verify(response, times(1)).addCookie(any());
    }

    @Test
    @DisplayName("조회수 증가 및 쿠키 생성 - 쿠키가 있는 경우")
    public void testValidateHitsCount_WithCookie() {
        // given
        ReviewDtlPageReviewDTO reviewDto = new ReviewDtlPageReviewDTO();
        reviewDto.setReviewId(1L);

        Cookie cookie = new Cookie("checkedReview", "[1]");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        HttpServletResponse response = mock(HttpServletResponse.class);

        Board review = new Board();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(review));

        // when
        boardService.validateHitsCount(reviewDto, request, response);

        // then
        verify(boardRepository, times(1)).findById(1L);
        verify(boardRepository, times(0)).save(review);
        verify(response, times(1)).addCookie(any());
    }

    @Test
    @DisplayName("조회수 증가 및 쿠키 생성 - 리뷰가 존재하지 않는 경우")
    public void testValidateHitsCount_EntityNotFoundException() {
        // given
        ReviewDtlPageReviewDTO reviewDto = new ReviewDtlPageReviewDTO();
        reviewDto.setReviewId(1L);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(boardRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.validateHitsCount(reviewDto, request, response);});

        // verify
        verify(boardRepository, times(1)).findById(1L);
        verify(response, never()).addCookie(any());
    }

    @Test
    @DisplayName("리뷰 삭제 - 성공")
    public void testDeleteReviews_Success() {
        // given
        Long[] reviewIds = {1L, 2L, 3L};

        Board review1 = new Board();
        review1.setId(1L);

        Board review2 = new Board();
        review2.setId(1L);

        Board review3 = new Board();
        review3.setId(1L);

        // 리뷰 ID가 존재하는 상황에서 삭제가 성공했다고 가정
        when(boardRepository.findById(1L)).thenReturn(Optional.of(review1));
        when(boardRepository.findById(2L)).thenReturn(Optional.of(review2));
        when(boardRepository.findById(3L)).thenReturn(Optional.of(review3));

        // when
        boardService.deletePosts(reviewIds);

        // then
        verify(boardRepository, times(3)).delete(any());
    }

    @Test
    @DisplayName("리뷰 삭제 - 실패 (리뷰가 존재하지 않는 경우)")
    public void testDeleteReviews_EntityNotFoundException() {
        // given
        Long[] reviewIds = {1L, 2L, 3L};

        when(boardRepository.findById(any())).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> {boardService.deletePosts(reviewIds);});

        // verify
        verify(boardRepository, never()).delete(any());
    }

    private NoticeDtlDtoInterface createMockNoticeDtlDto(Long boardId, String title, String content, boolean hasNotice, Long memberId, LocalDateTime regTime) {
        return new NoticeDtlDtoInterface() {
            @Override
            public Long getBoardId() {
                return boardId;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getContent() {
                return content;
            }

            @Override
            public boolean getHasNotice() {
                return hasNotice;
            }

            @Override
            public Long getMemberId() {
                return memberId;
            }

            @Override
            public LocalDateTime getRegTime() {
                return regTime;
            }
        };
    }

    private static QaEnrollDTO createQaEnrollDto() {
        return QaEnrollDTO.builder()
                .title("test")
                .content("test")
                .originNo(0L)
                .password("test1234")
                .hasPrivate(true)
                .build();
    }
}