package com.windsome.repository.member;

import com.windsome.dto.member.UserSummaryDTO;
import com.windsome.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member>, MemberRepositoryCustom {

    @Query("select new com.windsome.dto.member.UserSummaryDTO(m.id, m.name, m.availablePoints, m.totalEarnedPoints, m.totalUsedPoints) from Member m where m.userIdentifier =:userIdentifier")
    UserSummaryDTO getUserSummary(@Param("userIdentifier") String userIdentifier);

    boolean existsByUserIdentifier(String userIdentifier);

    boolean existsByEmail(String email);

    Member findByUserIdentifier(String userIdentifier);

    Member findByEmail(String email);

    Optional<Member> findByNameAndEmail(String name, String email);

    Optional<Member> findByUserIdentifierAndNameAndEmail(String userIdentifier, String name, String email);

    Optional<Member> findOneByEmail(String email);

    Optional<Member> findByName(String formatted);

    Optional<Member> findOneByUserIdentifier(String userIdentifier);
}
