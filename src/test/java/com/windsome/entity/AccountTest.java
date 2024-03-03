package com.windsome.entity;

import com.windsome.constant.Role;
import com.windsome.repository.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AccountTest {

    @Autowired MemberRepository memberRepository;
    @PersistenceContext EntityManager em;

    @Test
    @DisplayName("Auditing 테스트")
    @WithMockUser(username = "test", roles = "USER")
    public void auditingTest() {
        // Given
        Member member = saveMember();

        // When
        Member saveMember = memberRepository.save(member);
        em.flush();
        em.clear();

        // Then
        assertNotNull(saveMember.getRegTime());
        assertNotNull(saveMember.getUpdateTime());
    }

    public Member saveMember() {
        Member member = Member.builder()
                .userIdentifier("test1234")
                .password("test1234")
                .name("test")
                .email("test1234@naver.com")
                .zipcode("test")
                .addr("test")
                .addrDetail("test")
                .role(Role.USER)
                .availablePoints(0)
                .totalUsedPoints(0)
                .totalEarnedPoints(0)
                .build();
        return memberRepository.save(member);
    }
}