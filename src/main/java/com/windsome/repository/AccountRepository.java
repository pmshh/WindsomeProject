package com.windsome.repository;

import com.windsome.dto.MyPageInfoDto;
import com.windsome.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUserIdentifier(String userIdentifier);

    boolean existsByEmail(String email);

    Account findByUserIdentifier(String userIdentifier);

    Account findByEmail(String email);

    @Query(value = "select new com.windsome.dto.MyPageInfoDto(coalesce(sum(o.totalOrderPrice), 0), a.point) from Order o join Account a on o.account.id = a.id where o.account.id = :accountId and a.id = :accountId")
    MyPageInfoDto getMyPageInfo(@Param("accountId") Long accountId);

}
