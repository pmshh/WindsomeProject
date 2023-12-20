package com.windsome.account;

import com.windsome.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUserIdentifier(String userIdentifier);
    boolean existsByEmail(String email);
    Account findByUserIdentifier(String userIdentifier);
    Account findByEmail(String email);
}
