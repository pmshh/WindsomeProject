package com.windsome.service;

import com.windsome.constant.Role;
import com.windsome.dto.board.qa.CommentDTO;
import com.windsome.dto.board.qa.CommentEnrollDTO;
import com.windsome.dto.board.qa.CommentUpdateDTO;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Member;
import com.windsome.entity.board.Comment;
import com.windsome.repository.board.BoardRepository;
import com.windsome.repository.board.CommentRepository;
import com.windsome.service.board.CommentService;
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
    @Mock BoardRepository boardRepository;

    @InjectMocks
    CommentService commentService;

    @Test
    @DisplayName("댓글 등록 테스트")
    public void enrollCommentTest() {
        // given
        Member member = new Member();
        member.setId(1L);
        member.setUserIdentifier("test1234");

        Board board = new Board();
        board.setId(1L);

        CommentEnrollDTO commentEnrollDto = new CommentEnrollDTO();
        commentEnrollDto.setQaId(1L);
        commentEnrollDto.setContent("test content");

        Comment comment = Comment.toEntity(commentEnrollDto, board, member);
        comment.setId(1L);

        when(boardRepository.findById(anyLong())).thenReturn(java.util.Optional.of(board));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // when
        Long savedCommentId = commentService.enrollComment(commentEnrollDto, member);

        // then
        assertNotNull(savedCommentId);
        verify(boardRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

//    @Test
//    @DisplayName("댓글 조회 테스트")
//    public void getCommentDtoListTest() {
//        // given
//        Long qaId = 1L;
//
//        Member member1 = new Member();
//        member1.setId(1L);
//        member1.setRole(Role.USER);
//
//        Member member2 = new Member();
//        member2.setId(2L);
//        member2.setRole(Role.USER);
//
//        Board board = new Board();
//        board.setId(1L);
//        board.setMember(member1);
//
//        List<Comment> comments = Arrays.asList(
//                new Comment(1L, member1, board, "댓글 내용1", false),
//                new Comment(2L, member2, board, "댓글 내용2", true)
//        );
//
//        when(commentRepository.findAllByBoardId(anyLong())).thenReturn(comments);
//
//        // when
//        List<CommentDTO> commentDTOList = commentService.getCommentDtoList(qaId);
//
//        // then
//        assertEquals(2, commentDTOList.size());
//        assertEquals("댓글 내용1", commentDTOList.get(0).getContent());
//        assertFalse(commentDTOList.get(0).isHasPrivate());
//        assertEquals("댓글 내용2", commentDTOList.get(1).getContent());
//        assertTrue(commentDTOList.get(1).isHasPrivate());
//        verify(commentRepository, times(1)).findAllByBoardId(anyLong());
//    }

    @Test
    @DisplayName("댓글 수정/삭제 권한 검증 테스트 - 권한이 있는 사용자")
    public void validateCommentTestWithValidMember() {
        // given
        Member member = new Member();
        member.setId(1L);
        member.setUserIdentifier("test1234");
        member.setRole(Role.USER);

        Board board = new Board();
        board.setId(1L);
        board.setMember(member);

        Comment comment = new Comment(1L, member, board, "댓글 내용", false);

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
        member1.setRole(Role.USER);

        Member admin = new Member();
        admin.setId(1L);
        admin.setUserIdentifier("admin");
        admin.setRole(Role.ADMIN);

        Board board = new Board();
        board.setId(1L);
        board.setMember(member1);

        Comment comment = new Comment(1L, member1, board, "댓글 내용", false);

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
        member1.setRole(Role.USER);

        Member member2 = new Member();
        member2.setId(1L);
        member2.setUserIdentifier("user2");
        member2.setRole(Role.USER);

        Board board = new Board();
        board.setId(1L);
        board.setMember(member1);

        Comment comment = new Comment(1L, member1, board, "댓글 내용", false);

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
        member.setRole(Role.USER);

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
        member.setRole(Role.USER);

        Board board = new Board();
        board.setId(1L);
        board.setMember(member);

        CommentUpdateDTO commentUpdateDto = new CommentUpdateDTO(1L, "수정된 댓글 내용", true);
        Comment comment = new Comment(1L, member, board, "원래 댓글 내용", false);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        // when
        commentService.updateComment(commentUpdateDto);

        // then
        verify(commentRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).save(comment);
        assertEquals("수정된 댓글 내용", comment.getContent());
        assertTrue(comment.isHasPrivate());
    }

    @Test
    @DisplayName("댓글 수정 테스트 - 존재하지 않는 댓글")
    public void updateCommentTestWithNonExistingComment() {
        // given
        CommentUpdateDTO commentUpdateDto = new CommentUpdateDTO(1L, "수정된 댓글 내용", true);

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
