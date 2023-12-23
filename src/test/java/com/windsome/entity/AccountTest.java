package com.windsome.entity;

import com.windsome.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional
class AccountTest {

    @Autowired
    AccountRepository accountRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @DisplayName("Auditing 테스트")
    @WithMockUser(username = "test", roles = "USER")
    public void auditingTest() {
        Account account = new Account();
        accountRepository.save(account);

        em.flush();
        em.clear();

        Account findAccount = accountRepository.findById(account.getId())
                .orElseThrow(EntityNotFoundException::new);

        System.out.println("account.getRegTime() = " + account.getRegTime());
        System.out.println("account.getUpdateTime() = " + account.getUpdateTime());

    }
}