package com.windsome.repository.board;

import com.windsome.dto.board.notice.NoticeDtlDtoInterface;
import com.windsome.dto.board.qa.QaDtlDtoInterface;
import com.windsome.entity.board.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, QuerydslPredicateExecutor<Board>, BoardRepositoryCustom {

    /**
     * Notice
     */
    List<Board> findAllByHasNoticeOrderByRegTimeDesc(boolean isNotice);

    @Query(value = "select b.board_id as boardId, b.title, b.content, b.has_notice as hasNotice, b.member_id as memberId, b.reg_time as regTime from Board b where b.board_id = :boardId" +
            " union all (select b.board_id as boardId, b.title, b.content, b.has_notice as hasNotice, b.member_id as memberId, b.reg_time as regTime from Board b where b.board_id < :boardId and b.board_type = 'Notice' order by b.board_id desc limit 1)" +
            " union all (select b.board_id as boardId, b.title, b.content, b.has_notice as hasNotice, b.member_id as memberId, b.reg_time as regTime from Board b where b.board_id > :boardId and b.board_type = 'Notice' order by b.board_id asc limit 1)", nativeQuery = true)
    List<NoticeDtlDtoInterface> getNoticeDtl(@Param("boardId") Long boardId);

    /**
     * Q&A
     */
    List<Board> findByOriginNoAndGroupOrderGreaterThan(Long originNo, int num);

    @Query(value = "select b.board_id as boardId, b.title, b.content, b.password, b.has_private as hasPrivate, b.reg_time as regTime, b.member_id as memberId from board b where b.board_id = :boardId and b.board_type = 'Q&A'" +
            " union all (select b.board_id as boardId, b.title, b.content, b.password, b.has_private as hasPrivate, b.reg_time as regTime, b.member_id as memberId from board b where b.board_id < :boardId and b.board_type = 'Q&A' order by b.board_id desc limit 1)" +
            " union all (select b.board_id as boardId, b.title, b.content, b.password, b.has_private as hasPrivate, b.reg_time as regTime, b.member_id as memberId from board b where b.board_id > :boardId and b.board_type = 'Q&A' order by b.board_id asc limit 1)", nativeQuery = true)
    List<QaDtlDtoInterface> getQaDtl(@Param("boardId") Long boardId);

    @Query(value = "select count(*) from Board b where b.boardType = 'Q&A'")
    long getTotalQaPosts();

    /**
     * Review
     */
    List<Board> findByProductIdOrderByIdDesc(Long productId, Pageable pageable);

    Long countByProductId(Long productId);

    @Query(value = "select avg(b.rating) from Board b where b.product.id = :productId")
    BigDecimal getRatingAvg(@Param("productId") Long productId);

    boolean existsByProductIdAndMemberId(Long productId, Long memberId);

    /**
     * Common
     */
    Board findByMemberId(Long id);
}
