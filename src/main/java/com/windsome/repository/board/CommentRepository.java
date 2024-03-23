package com.windsome.repository.board;

import com.windsome.entity.board.Board;
import com.windsome.entity.board.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByBoardId(Long boardId);
}
