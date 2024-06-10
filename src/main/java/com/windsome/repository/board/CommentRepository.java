package com.windsome.repository.board;

import com.windsome.entity.board.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "select c from Comment c where c.member.isDeleted = false")
    List<Comment> findAllActiveCommentsByBoardId(Long boardId);
}
