package com.windsome.service.board;

import com.windsome.constant.Role;
import com.windsome.dto.board.qa.CommentDTO;
import com.windsome.dto.board.qa.CommentEnrollDTO;
import com.windsome.dto.board.qa.CommentUpdateDTO;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Member;
import com.windsome.entity.board.Comment;
import com.windsome.repository.board.BoardRepository;
import com.windsome.repository.board.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    /**
     * 댓글 등록
     */
    public Long enrollComment(CommentEnrollDTO commentEnrollDto, Member member) {
        Board qa = boardRepository.findById(commentEnrollDto.getQaId()).orElseThrow(EntityNotFoundException::new);
        Comment comment = Comment.toEntity(commentEnrollDto, qa, member);
        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    /**
     * 댓글 조회
     */
    public List<CommentDTO> getCommentDtoList(Long qaId) {
        return commentRepository.findAllActiveCommentsByBoardId(qaId).stream()
                .map(comment -> new CommentDTO(comment.getId(), comment.getMember().getUserIdentifier(), comment.getMember().getName(), comment.getMember().getRole(), comment.getRegTime(), comment.getContent(), comment.isHasPrivate()))
                .collect(Collectors.toList());
    }

    /**
     * 댓글 수정/삭제 권한 검증
     */
    public boolean validateComment(Member member, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
        // 댓글 작성자 본인이거나, 관리자인 경우 삭제 가능
        return !(comment.getMember().getUserIdentifier().equals(member.getUserIdentifier()) || member.getRole() == Role.ADMIN);
    }

    /**
     * 댓글 수정
     */
    public void updateComment(CommentUpdateDTO commentUpdateDto) {
        Comment comment = commentRepository.findById(commentUpdateDto.getCommentId()).orElseThrow(EntityNotFoundException::new);
        comment.setContent(commentUpdateDto.getContent());
        comment.setHasPrivate(commentUpdateDto.isHasPrivate());
        commentRepository.save(comment);
    }

    /**
     * 댓글 삭제
     */
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
        commentRepository.delete(comment);
    }
}
