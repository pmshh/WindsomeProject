package com.windsome.repository;

import com.windsome.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUserIdentifier(String userIdentifier);

    boolean existsByEmail(String email);

    Account findByUserIdentifier(String userIdentifier);

    Account findByEmail(String email);
}
