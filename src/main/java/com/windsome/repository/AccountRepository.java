package com.windsome.repository;

import com.windsome.dto.account.MyPageInfoDto;
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

    @Query(value = "select new com.windsome.dto.account.MyPageInfoDto(a.id, a.name, a.totalOrderPrice, a.point, a.totalPoint, a.totalUsePoint) from Account a where a.userIdentifier = :userIdentifier")
    MyPageInfoDto getMyPageInfo(@Param("userIdentifier") String userIdentifier);
}
