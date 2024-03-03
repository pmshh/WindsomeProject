package com.windsome.service.board;

import com.windsome.constant.Role;
import com.windsome.dto.board.notice.*;
import com.windsome.entity.Member;
import com.windsome.entity.board.Notice;
import com.windsome.repository.board.notice.NoticeRepository;
import com.windsome.repository.member.MemberRepository;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class NoticeServiceTest {

    @Mock NoticeRepository noticeRepository;
    @Mock MemberRepository memberRepository;

    @InjectMocks NoticeService noticeService;

    @Test
    @DisplayName("일반 공지사항 조회")
    public void testGetNoticeList() {
        // given
        NoticeSearchDto noticeSearchDto = new NoticeSearchDto();
        Pageable pageable = Pageable.ofSize(10).withPage(0);

        List<NoticeListDto> fakeNoticeList = Collections.singletonList(new NoticeListDto());
        Page<NoticeListDto> fakePage = new PageImpl<>(fakeNoticeList, pageable, fakeNoticeList.size());
        when(noticeRepository.getNoticeList(noticeSearchDto, pageable)).thenReturn(fakePage);

        // when
        Page<NoticeListDto> resultPage = noticeService.getNoticeList(noticeSearchDto, pageable);

        // then
        verify(noticeRepository, times(1)).getNoticeList(noticeSearchDto, pageable);
        assertEquals(fakePage, resultPage);
        assertEquals(resultPage.getSize(), 10);
    }

    @Test
    @DisplayName("상단 고정 공지사항 조회")
    public void testGetFixTopNoticeList() {
        // given
        List<Notice> expectedNotices = Arrays.asList(new Notice(), new Notice());

        when(noticeRepository.findAllByNoticeYNOrderByRegTimeDesc(true)).thenReturn(expectedNotices);

        // when
        List<Notice> actualNotices = noticeService.getFixTopNoticeList();

        // then
        verify(noticeRepository, times(1)).findAllByNoticeYNOrderByRegTimeDesc(true);
        assertEquals(expectedNotices, actualNotices);
    }

    @Test
    @DisplayName("공지사항 등록")
    public void testEnrollNotice() {
        // given
        NoticeDto noticeDto = new NoticeDto();
        Member member = new Member();

        Notice notice = new Notice();
        notice.setId(1L);
        notice.setMember(member);

        when(noticeRepository.save(any())).thenReturn(notice);

        // when
        Long savedNoticeId = noticeService.enrollNotice(noticeDto, member);

        // then
        verify(noticeRepository, times(1)).save(any());
        assertEquals(1L, savedNoticeId);
    }

    @Test
    @DisplayName("공지사항 상세 화면 조회")
    public void testGetNoticeDtl() {
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

        when(noticeRepository.getNoticeDtl(anyLong())).thenReturn(noticeDtlDtoInterfaces);
        when(memberRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long memberId = invocation.getArgument(0);
            if (memberId.equals(1L)) {
                Member member = new Member();
                member.setId(1L);
                member.setName("회원1");
                return Optional.of(member);
            } else if (memberId.equals(2L)) {
                Member member = new Member();
                member.setId(2L);
                member.setName("회원2");
                return Optional.of(member);
            } else {
                throw new EntityNotFoundException();
            }
        });

        // when
        List<NoticeDtlDto> result = noticeService.getNoticeDtl(noticeId);

        // then
        verify(noticeRepository, times(1)).getNoticeDtl(noticeId);
        verify(memberRepository, times(2)).findById(anyLong());
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
        NoticeUpdateDto noticeUpdateDto = new NoticeUpdateDto();
        noticeUpdateDto.setTitle("수정된 제목");
        noticeUpdateDto.setContent("수정된 내용");

        Notice existingNotice = new Notice();
        existingNotice.setId(noticeId);
        existingNotice.setTitle("기존 제목");
        existingNotice.setContent("기존 내용");
        existingNotice.setRegTime(LocalDateTime.now());

        when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(existingNotice));

        // when
        noticeService.updateNotice(noticeId, noticeUpdateDto);

        // then
        verify(noticeRepository, times(1)).findById(noticeId);
        verify(noticeRepository, times(1)).save(existingNotice);
        assertEquals("수정된 제목", existingNotice.getTitle());
        assertEquals("수정된 내용", existingNotice.getContent());
    }

    @Test
    @DisplayName("공지사항 수정 - 공지사항을 찾을 수 없는 경우")
    public void testUpdateNotice_NotFound() {
        // given
        Long noticeId = 1L;
        NoticeUpdateDto noticeUpdateDto = new NoticeUpdateDto();

        when(noticeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> noticeService.updateNotice(noticeId, noticeUpdateDto));
        verify(noticeRepository, times(1)).findById(noticeId);
        verifyNoMoreInteractions(noticeRepository);
    }

    @Test
    @DisplayName("공지사항 조회 - 성공")
    public void testGetNotice_Success() {
        // given
        Long noticeId = 1L;
        Notice expectedNotice = new Notice();
        expectedNotice.setId(noticeId);
        expectedNotice.setTitle("제목");
        expectedNotice.setContent("내용");

        when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(expectedNotice));

        // when
        Notice result = noticeService.getNotice(noticeId);

        // then
        verify(noticeRepository, times(1)).findById(noticeId);
        assertEquals(expectedNotice, result);
    }

    @Test
    @DisplayName("공지사항 조회 - 공지사항을 찾을 수 없는 경우")
    public void testGetNotice_NotFound() {
        // given
        Long noticeId = 1L;

        when(noticeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> noticeService.getNotice(noticeId));
        verify(noticeRepository, times(1)).findById(noticeId);
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    public void testDeleteNotice_Success() {
        // given
        Long noticeId = 1L;
        Notice mockNotice = new Notice();
        mockNotice.setId(noticeId);

        when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(mockNotice));

        // when
        noticeService.deleteNotice(noticeId);

        // then
        verify(noticeRepository, times(1)).findById(noticeId);
        verify(noticeRepository, times(1)).deleteById(noticeId);
    }

    @Test
    @DisplayName("게시글 삭제 - 공지사항을 찾을 수 없는 경우")
    public void testDeleteNotice_NotFound() {
        // given
        Long noticeId = 1L;

        when(noticeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> noticeService.deleteNotice(noticeId));
        verify(noticeRepository, times(1)).findById(noticeId);
        verify(noticeRepository, times(0)).deleteById(noticeId);
    }

    @Test
    @DisplayName("관리자 권한 - 관리자일 때")
    public void testIsAdmin_Admin() {
        // given
        Member mockMember = mock(Member.class);
        when(mockMember.getRole()).thenReturn(Role.ADMIN);

        // when
        boolean result = noticeService.isAdmin(mockMember);

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
        boolean result = noticeService.isAdmin(mockMember);

        // then
        assertFalse(result);
        verify(mockMember).getRole();
    }

    @Test
    @DisplayName("게시글 여러건 삭제 - 성공")
    public void testDeleteNotices_Success() {
        // given
        Long[] noticeIds = {1L, 2L, 3L};
        Notice notice1 = new Notice();
        Notice notice2 = new Notice();
        Notice notice3 = new Notice();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice1));
        when(noticeRepository.findById(2L)).thenReturn(Optional.of(notice2));
        when(noticeRepository.findById(3L)).thenReturn(Optional.of(notice3));

        // when
        noticeService.deleteNotices(noticeIds);

        // then
        verify(noticeRepository, times(3)).findById(any());
        verify(noticeRepository, times(3)).delete(any());
    }

    @Test
    @DisplayName("게시글 여러건 삭제 - 존재하지 않는 게시글")
    public void testDeleteNotices_EntityNotFoundException() {
        // given
        Long[] noticeIds = {1L, 2L, 3L};
        when(noticeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> noticeService.deleteNotices(noticeIds));
    }

    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 가능")
    public void testCheckNoticeYN_NoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;

        Notice notice = new Notice();
        notice.setNoticeYN(false);

        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        // when
        boolean result = noticeService.checkNoticeYN(noticeId, noticeYN);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 불가능(이미 공지글로 설정된 경우)")
    public void testCheckNoticeYN_NotNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;

        Notice notice = new Notice();
        notice.setNoticeYN(true);

        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        // when
        boolean result = noticeService.checkNoticeYN(noticeId, noticeYN);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("공지글 설정 가능 여부 검증 - 존재하지 않는 공지글")
    public void testCheckNoticeYN_EntityNotFoundException() {
        // given
        Long noticeId = 1L;
        boolean noticeYN = true;
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> noticeService.checkNoticeYN(noticeId, noticeYN));
    }

    @Test
    @DisplayName("공지글 설정 수정 - 공지글로 변경")
    public void testUpdateNoticeYN_SetNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = true;

        Notice notice = new Notice();
        notice.setNoticeYN(false);

        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        // when
        noticeService.updateNoticeYN(noticeId, noticeYn);

        // then
        assertTrue(notice.isNoticeYN());
        verify(noticeRepository).save(notice);
    }

    @Test
    @DisplayName("공지글 설정 수정 - 공지글 해제")
    public void testUpdateNoticeYN_UnsetNoticeYN() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = false;

        Notice notice = new Notice();
        notice.setNoticeYN(true);

        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));

        // when
        noticeService.updateNoticeYN(noticeId, noticeYn);

        // then
        assertFalse(notice.isNoticeYN());
        verify(noticeRepository).save(notice);
    }

    @Test
    @DisplayName("공지글 설정 수정 - 존재하지 않는 공지글")
    public void testUpdateNoticeYN_EntityNotFoundException() {
        // given
        Long noticeId = 1L;
        boolean noticeYn = true;

        when(noticeRepository.findById(noticeId)).thenReturn(Optional.empty());

        // when, then
        assertThrows(EntityNotFoundException.class, () -> noticeService.updateNoticeYN(noticeId, noticeYn));
        verify(noticeRepository, never()).save(any());
    }

    private NoticeDtlDtoInterface createMockNoticeDtlDto(Long noticeId, String title, String content, boolean noticeYN, Long memberId, LocalDateTime regTime) {
        return new NoticeDtlDtoInterface() {
            @Override
            public Long getNoticeId() {
                return noticeId;
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
            public boolean getNoticeYN() {
                return noticeYN;
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
}