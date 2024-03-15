package com.windsome.service.board;

import com.windsome.constant.Role;
import com.windsome.dto.board.qa.*;
import com.windsome.entity.member.Member;
import com.windsome.entity.board.Qa;
import com.windsome.repository.board.qa.QaRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.windsome.TestUtil.createMember;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class QaServiceTest {

    @Mock private QaRepository qaRepositoryMock;
    @Mock private CommentService commentServiceMock;
    @InjectMocks private QaService qaService;

    @Test
    @DisplayName("게시글 조회 테스트")
    public void getQaListTest() {
        // Given
        QaSearchDto qaSearchDto = new QaSearchDto();
        Pageable pageable = Pageable.unpaged();

        QaListDto qaListDto = new QaListDto();
        Page<QaListDto> expectedPage = new PageImpl<>(Collections.singletonList(qaListDto));

        when(qaRepositoryMock.getQaList(qaSearchDto, pageable)).thenReturn(expectedPage);

        // When
        Page<QaListDto> resultPage = qaService.getQaList(qaSearchDto, pageable);

        // Then
        assertEquals(expectedPage, resultPage);
        verify(qaRepositoryMock, times(1)).getQaList(qaSearchDto, pageable);
        verifyNoMoreInteractions(qaRepositoryMock);
    }

    @Test
    @DisplayName("게시글 등록 테스트 - originNo가 0인 경우")
    public void enrollQa_OriginNoIsZero() {
        // Given
        QaEnrollDto qaEnrollDto = createQaEnrollDto();
        Member member = createMember(1L);

        // When
        qaService.enrollQa(qaEnrollDto, member);

        // Then
        verify(qaRepositoryMock, times(2)).save(any(Qa.class)); // save 메서드가 호출되었는지 검증
    }

    @Test
    @DisplayName("게시글 등록 테스트 - originNo가 0이 아닌 경우")
    public void enrollQa_OriginNoIsNotZero() {
        // Given
        Member member = createMember(1L);
        QaEnrollDto qaEnrollDto = createQaEnrollDto();
        qaEnrollDto.setOriginNo(1L);

        Qa originQa = new Qa(); // 원글 QA
        originQa.setId(1L);
        originQa.setOriginNo(1L);
        originQa.setGroupOrd(0);
        originQa.setGroupLayer(0);

        when(qaRepositoryMock.findById(1L)).thenReturn(Optional.of(originQa)); // findById 메서드가 호출될 때 가짜 데이터 반환하도록 설정

        // When
        qaService.enrollQa(qaEnrollDto, member);

        // Then
        verify(qaRepositoryMock, times(1)).save(any(Qa.class)); // save 메서드가 호출되었는지 검증
        verify(qaRepositoryMock, times(1)).findByOriginNoAndGroupOrdGreaterThan(originQa.getOriginNo(), originQa.getGroupOrd()); // findByOriginNoAndGroupOrdGreaterThan 메서드가 호출되었는지 검증
        verify(qaRepositoryMock, times(1)).save(argThat(qa -> qa.getGroupOrd() == 1));
        verify(qaRepositoryMock, times(1)).save(argThat(qa -> qa.getGroupLayer() == 1));
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 - 관리자일시 true 반환하는지 확인")
    public void validatePost_AdminRole_ReturnsTrue() {
        // Given
        Member member = createMember(1L);
        member.setRole(Role.ADMIN); // 관리자 권한 설정

        // When
        boolean result = qaService.validatePost(member, 1L, "password");

        // Then
        assertFalse(result); // 관리자 권한인 경우 항상 false 반환
        verify(qaRepositoryMock, never()).findById(anyLong()); // 관리자 권한인 경우 findById 메서드가 호출되지 않았는지 검증
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

        Qa qa = new Qa();
        qa.setId(1L);
        qa.setOriginNo(1L);
        qa.setMember(member1);
        qa.setPassword("correctPassword"); // 올바른 비밀번호 설정
        when(qaRepositoryMock.findById(1L)).thenReturn(java.util.Optional.of(qa));

        // When
        boolean result = qaService.validatePost(member2, 1L, "wrongPassword");

        // Then
        assertTrue(result); // 잘못된 비밀번호일 경우 true 반환
        verify(qaRepositoryMock, times(3)).findById(1L); // findById 메서드가 2번 호출되었는지 검증
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

        Qa qa = new Qa();
        qa.setId(1L);
        qa.setOriginNo(1L);
        qa.setMember(member1);
        qa.setPassword("correctPassword"); // 올바른 비밀번호 설정
        when(qaRepositoryMock.findById(1L)).thenReturn(java.util.Optional.of(qa));

        // When
        boolean result = qaService.validatePost(member2, 1L, "correctPassword");

        // Then
        assertFalse(result); // 올바른 비밀번호일 경우 false 반환
        verify(qaRepositoryMock, times(3)).findById(1L); // findById 메서드가 1번 호출되었는지 검증
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

        Qa qa = new Qa();
        qa.setId(1L);
        qa.setOriginNo(1L);
        qa.setMember(member1);
        qa.setPassword("correctPassword"); // 올바른 비밀번호 설정
        when(qaRepositoryMock.findById(1L)).thenReturn(java.util.Optional.of(qa));


        // When
        boolean result = qaService.validatePost(member2, 1L, null); // 비밀번호가 null인 경우

        // Then
        assertTrue(result); // 비밀번호가 null일 경우 true 반환
        verify(qaRepositoryMock, times(2)).findById(anyLong()); // findById 메서드가 호출되지 않았는지 검증
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    public void deleteQa_DeletesQaSuccessfully() {
        // Given
        Long qaId = 1L;
        Qa qa = new Qa();
        qa.setId(qaId);

        // findById 메소드에 대한 스텁 설정
        when(qaRepositoryMock.findById(qaId)).thenReturn(java.util.Optional.of(qa));

        // When
        qaService.deleteQa(qaId);

        // Then
        verify(qaRepositoryMock, times(1)).findById(qaId); // findById 메소드가 호출됐는지 검증
        verify(qaRepositoryMock, times(1)).delete(qa); // delete 메소드가 호출됐는지 검증
    }

    @Test
    @DisplayName("선택/전체 게시글 삭제 테스트")
    public void deleteQas_DeletesAllQasSuccessfully() {
        // Given
        Long[] qaIds = {1L, 2L, 3L}; // 테스트용 qaIds 배열

        // findById 메소드에 대한 스텁 설정
        Qa qa = new Qa(); // 실제 Q&A 객체 생성
        for (Long qaId : qaIds) {
            when(qaRepositoryMock.findById(qaId)).thenReturn(java.util.Optional.of(qa));
        }

        // When
        qaService.deleteQas(qaIds);

        // Then
        for (Long qaId : qaIds) {
            verify(qaRepositoryMock, times(1)).findById(qaId); // findById 메서드가 호출됐는지 검증
        }

        verify(qaRepositoryMock, times(3)).delete(qa); // delete 메서드가 호출됐는지 검증
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 테스트 - 올바른 비밀번호 입력")
    void validatePostPassword_ValidPassword_ReturnsTrue() {
        // Given
        Long qaId = 1L;
        String password = "correctPassword";
        Qa qa = new Qa();
        qa.setPassword(password);
        when(qaRepositoryMock.findById(qaId)).thenReturn(Optional.of(qa));

        // When
        boolean result = qaService.validatePostPassword(qaId, password);

        // Then
        assertTrue(result);
        verify(qaRepositoryMock, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 테스트 - 잘못된 비밀번호 입력")
    void validatePostPassword_InvalidPassword_ReturnsFalse() {
        // Given
        Long qaId = 1L;
        String password = "incorrectPassword";
        Qa qa = new Qa();
        qa.setPassword("correctPassword"); // 실제 비밀번호와 다른 비밀번호 설정
        when(qaRepositoryMock.findById(qaId)).thenReturn(Optional.of(qa));

        // When
        boolean result = qaService.validatePostPassword(qaId, password);

        // Then
        assertFalse(result);
        verify(qaRepositoryMock, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("게시글 비밀번호 검증 테스트 - EntityNotFoundException 발생하는지 확인")
    void validatePostPassword_QaNotFound_ThrowsException() {
        // Given
        Long qaId = 1L;
        when(qaRepositoryMock.findById(qaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> qaService.validatePostPassword(qaId, "password"));
        verify(qaRepositoryMock, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 성공")
    void updateQa_QaExists_UpdateSuccessful() {
        // Given
        QaUpdateDto qaUpdateDto = new QaUpdateDto();
        qaUpdateDto.setQaId(1L);
        qaUpdateDto.setTitle("Updated Title");
        qaUpdateDto.setContent("Updated Content");

        Qa existingQa = new Qa();
        existingQa.setId(1L);
        existingQa.setTitle("Old Title");
        existingQa.setContent("Old Content");

        when(qaRepositoryMock.findById(qaUpdateDto.getQaId())).thenReturn(Optional.of(existingQa));

        // When
        qaService.updateQa(qaUpdateDto);

        // Then
        verify(qaRepositoryMock, times(1)).findById(qaUpdateDto.getQaId());
        verify(qaRepositoryMock, times(1)).save(any(Qa.class));

        assertEquals("Updated Title", existingQa.getTitle());
        assertEquals("Updated Content", existingQa.getContent());
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 실패(존재하지 않는 게시글 접근)")
    void updateQa_QaNotFound_ThrowsException() {
        // Given
        QaUpdateDto qaUpdateDto = new QaUpdateDto();
        qaUpdateDto.setQaId(1L);
        qaUpdateDto.setTitle("Updated Title");
        qaUpdateDto.setContent("Updated Content");

        when(qaRepositoryMock.findById(qaUpdateDto.getQaId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> qaService.updateQa(qaUpdateDto));
        verify(qaRepositoryMock, times(1)).findById(qaUpdateDto.getQaId());
        verify(qaRepositoryMock, never()).save(any(Qa.class));
    }

    @Test
    @DisplayName("단일 게시글 상세 조회 테스트 - 성공")
    void getQaForUpdate_QaExists_ReturnsDto() {
        // Given
        Long qaId = 1L;
        Qa qa = new Qa();
        qa.setId(qaId);
        qa.setTitle("Test Title");
        qa.setContent("Test Content");

        when(qaRepositoryMock.findById(qaId)).thenReturn(Optional.of(qa));

        // When
        QaUpdateDto resultDto = qaService.getQaForUpdate(qaId);

        // Then
        assertNotNull(resultDto);
        assertEquals(qaId, resultDto.getQaId());
        assertEquals("Test Title", resultDto.getTitle());
        assertEquals("Test Content", resultDto.getContent());

        verify(qaRepositoryMock, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("단일 게시글 상세 조회 테스트 - EntityNotFoundException 발생하는지 확인")
    void getQaForUpdate_QaNotFound_ThrowsException() {
        // Given
        Long qaId = 1L;
        when(qaRepositoryMock.findById(qaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> qaService.getQaForUpdate(qaId));

        verify(qaRepositoryMock, times(1)).findById(qaId);
    }

    @Test
    @DisplayName("게시글 댓글 조회 테스트")
    void getCommentList_ReturnsListOfCommentDto() {
        // Given
        Long qaId = 1L;
        List<CommentDto> expectedComments = new ArrayList<>();
        expectedComments.add(new CommentDto("Comment 1"));
        expectedComments.add(new CommentDto("Comment 2"));

        when(commentServiceMock.getCommentDtoList(qaId)).thenReturn(expectedComments);

        // When
        List<CommentDto> actualComments = qaService.getCommentList(qaId);

        // Then
        assertEquals(expectedComments.size(), actualComments.size());
        for (int i = 0; i < expectedComments.size(); i++) {
            assertEquals(expectedComments.get(i).getContent(), actualComments.get(i).getContent());
        }

        verify(commentServiceMock, times(1)).getCommentDtoList(qaId);
    }

    private static QaEnrollDto createQaEnrollDto() {
        return QaEnrollDto.builder()
                .title("test")
                .content("test")
                .originNo(0L)
                .password("test1234")
                .secretYN(true)
                .build();
    }
}