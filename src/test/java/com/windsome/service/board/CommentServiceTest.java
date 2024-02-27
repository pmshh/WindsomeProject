package com.windsome.service.board;

import com.windsome.constant.Role;
import com.windsome.dto.board.qa.CommentDto;
import com.windsome.dto.board.qa.CommentEnrollDto;
import com.windsome.dto.board.qa.CommentUpdateDto;
import com.windsome.entity.Member;
import com.windsome.entity.board.Comment;
import com.windsome.entity.board.Qa;
import com.windsome.repository.board.qa.CommentRepository;
import com.windsome.repository.board.qa.QaRepository;
import com.windsome.repository.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock QaRepository qaRepository;

    @InjectMocks CommentService commentService;

    @Test
    @DisplayName("댓글 등록 테스트")
    public void enrollCommentTest() {
        // given
        Member member = new Member();
        member.setId(1L);
        member.setUserIdentifier("test1234");

        Qa qa = new Qa();
        qa.setId(1L);

        CommentEnrollDto commentEnrollDto = new CommentEnrollDto();
        commentEnrollDto.setQaId(1L);
        commentEnrollDto.setContent("test content");

        Comment comment = Comment.toEntity(commentEnrollDto, qa, member);
        comment.setId(1L);

        when(qaRepository.findById(anyLong())).thenReturn(java.util.Optional.of(qa));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // when
        Long savedCommentId = commentService.enrollComment(commentEnrollDto, member);

        // then
        assertNotNull(savedCommentId);
        verify(qaRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 조회 테스트")
    public void getCommentDtoListTest() {
        // given
        Long qaId = 1L;

        Member member1 = new Member();
        member1.setId(1L);
        member1.setState(Role.USER);

        Member member2 = new Member();
        member2.setId(2L);
        member2.setState(Role.USER);

        Qa qa = new Qa();
        qa.setId(1L);
        qa.setMember(member1);

        List<Comment> comments = Arrays.asList(
                new Comment(1L, member1, qa, "댓글 내용1", false),
                new Comment(2L, member2, qa, "댓글 내용2", true)
        );

        when(commentRepository.findByQaId(anyLong())).thenReturn(comments);

        // when
        List<CommentDto> commentDtoList = commentService.getCommentDtoList(qaId);

        // then
        assertEquals(2, commentDtoList.size());
        assertEquals("댓글 내용1", commentDtoList.get(0).getContent());
        assertFalse(commentDtoList.get(0).isSecretYN());
        assertEquals("댓글 내용2", commentDtoList.get(1).getContent());
        assertTrue(commentDtoList.get(1).isSecretYN());
        verify(commentRepository, times(1)).findByQaId(anyLong());
    }

    @Test
    @DisplayName("댓글 수정/삭제 권한 검증 테스트 - 권한이 있는 사용자")
    public void validateCommentTestWithValidMember() {
        // given
        Member member = new Member();
        member.setId(1L);
        member.setUserIdentifier("test1234");
        member.setState(Role.USER);

        Qa qa = new Qa();
        qa.setId(1L);
        qa.setMember(member);

        Comment comment = new Comment(1L, member, qa, "댓글 내용", false);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        // when
        boolean result = commentService.validateComment(member, 1L);

        // then
        assertFalse(result);
        verify(commentRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("댓글 수정/삭제 권한 검증 테스트 - 관리자 접근")
    public void validateCommentTestWithAdmin() {
        // given
        Member member1 = new Member();
        member1.setId(1L);
        member1.setUserIdentifier("user");
        member1.setState(Role.USER);

        Member admin = new Member();
        admin.setId(1L);
        admin.setUserIdentifier("admin");
        admin.setState(Role.ADMIN);

        Qa qa = new Qa();
        qa.setId(1L);
        qa.setMember(member1);

        Comment comment = new Comment(1L, member1, qa, "댓글 내용", false);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        // when
        boolean result = commentService.validateComment(admin, 1L);

        // then
        assertFalse(result);
        verify(commentRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("댓글 수정/삭제 권한 검증 테스트 - 권한이 없는 사용자")
    public void validateCommentTestWithInvalidMember() {
        // given
        Member member1 = new Member();
        member1.setId(1L);
        member1.setUserIdentifier("user1");
        member1.setState(Role.USER);

        Member member2 = new Member();
        member2.setId(1L);
        member2.setUserIdentifier("user2");
        member2.setState(Role.USER);

        Qa qa = new Qa();
        qa.setId(1L);
        qa.setMember(member1);

        Comment comment = new Comment(1L, member1, qa, "댓글 내용", false);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        // when
        boolean result = commentService.validateComment(member2, 1L);

        // then
        assertTrue(result);
        verify(commentRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("댓글 수정/삭제 권한 검증 테스트 - 존재하지 않는 댓글")
    public void validateCommentTestWithNonExistingComment() {
        // given
        Member member = new Member();
        member.setId(1L);
        member.setUserIdentifier("test1234");
        member.setState(Role.USER);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> {
            commentService.validateComment(member, 1L);
        });
        verify(commentRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("댓글 수정 테스트 - 존재하는 댓글")
    public void updateCommentTestWithExistingComment() {
        // given
        Member member = new Member();
        member.setId(1L);
        member.setUserIdentifier("test1234");
        member.setState(Role.USER);

        Qa qa = new Qa();
        qa.setId(1L);
        qa.setMember(member);

        CommentUpdateDto commentUpdateDto = new CommentUpdateDto(1L, "수정된 댓글 내용", true);
        Comment comment = new Comment(1L, member, qa, "원래 댓글 내용", false);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        // when
        commentService.updateComment(commentUpdateDto);

        // then
        verify(commentRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).save(comment);
        assertEquals("수정된 댓글 내용", comment.getContent());
        assertTrue(comment.isSecretYN());
    }

    @Test
    @DisplayName("댓글 수정 테스트 - 존재하지 않는 댓글")
    public void updateCommentTestWithNonExistingComment() {
        // given
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto(1L, "수정된 댓글 내용", true);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> {
            commentService.updateComment(commentUpdateDto);
        });
        verify(commentRepository, times(1)).findById(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    public void testDeleteComment_Success() {
        // given
        Long commentId = 1L;
        Comment comment = new Comment();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commentId);

        // then
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("댓글 삭제 - 실패 (존재하지 않는 댓글)")
    public void testDeleteComment_EntityNotFoundException() {
        // given
        Long commentId = 1L;
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {commentService.deleteComment(commentId);});

        // verify
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }
}
