package com.windsome.repository.board.notice;

import com.windsome.dto.board.notice.NoticeDtlDtoInterface;
import com.windsome.entity.board.Notice;
import com.windsome.entity.board.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, QuerydslPredicateExecutor<Review>, NoticeRepositoryCustom {

    List<Notice> findAllByNoticeYNOrderByRegTimeDesc(boolean noticeYN);

    @Query(value = "select n.notice_id as noticeId, n.title, n.content, n.noticeyn as noticeYN, n.member_id as memberId, n.reg_time as regTime from Notice n where n.notice_id = :noticeId" +
            " union all (select n.notice_id as noticeId, n.title, n.content, n.noticeyn as noticeYN, n.member_id as memberId, n.reg_time as regTime from Notice n where n.notice_id < :noticeId order by n.notice_id desc limit 1)" +
            " union all (select n.notice_id as noticeId, n.title, n.content, n.noticeyn as noticeYN, n.member_id as memberId, n.reg_time as regTime from Notice n where n.notice_id > :noticeId order by n.notice_id asc limit 1)", nativeQuery = true)
    List<NoticeDtlDtoInterface> getNoticeDtl(@Param("noticeId") Long noticeId);
}
