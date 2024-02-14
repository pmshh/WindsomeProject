package com.windsome.repository.board.qa;

import com.windsome.dto.board.qa.QaDtlDtoInterface;
import com.windsome.entity.board.Qa;
import com.windsome.entity.board.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QaRepository extends JpaRepository<Qa, Long>, QuerydslPredicateExecutor<Review>, QaRepositoryCustom {

    List<Qa> findByOriginNoAndGroupOrdGreaterThan(Long originNo, int num);

    @Query(value = "select q.qa_id as qaId, q.title, q.content, q.password, q.secretyn, q.reg_time as regTime, q.account_id as accountId from qa q where q.qa_id = :qaId" +
            " union all (select q.qa_id as qaId, q.title, q.content, q.password, q.secretyn, q.reg_time as regTime, q.account_id as accountId from qa q where q.qa_id < :qaId order by q.qa_id desc limit 1)" +
            " union all (select q.qa_id as qaId, q.title, q.content, q.password, q.secretyn, q.reg_time as regTime, q.account_id as accountId from qa q where q.qa_id > :qaId order by q.qa_id asc limit 1)", nativeQuery = true)
    List<QaDtlDtoInterface> getQaDtl(@Param("qaId") Long qaId);
}
