package com.windsome.service;

import com.windsome.WithAccount;
import com.windsome.dto.ProfileFormDto;
import com.windsome.dto.SignUpFormDto;
import com.windsome.entity.Account;
import com.windsome.repository.AccountRepository;
import com.windsome.service.mail.ConsoleEmailService;
import com.windsome.service.mail.HtmlMailSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;


@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AccountServiceTest {

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원 등록 테스트")
    public void saveNewAccount() {
        // given
        SignUpFormDto signUpFormDto = getSignUpFormDto();

        // when
        Account account = accountService.saveNewAccount(signUpFormDto);
        Account savedAccount = accountRepository.findById(account.getId()).orElseThrow(EntityNotFoundException::new);

        // then
        assertNotNull(savedAccount);
        assertEquals(savedAccount.getUserIdentifier(), signUpFormDto.getUserIdentifier());
        assertEquals(savedAccount.getEmail(), signUpFormDto.getEmail());
        assertEquals(savedAccount.getName(), signUpFormDto.getName());
        assertEquals(savedAccount.getAddress1(), signUpFormDto.getAddress1());
        assertEquals(savedAccount.getAddress2(), signUpFormDto.getAddress2());
        assertEquals(savedAccount.getAddress3(), signUpFormDto.getAddress3());
        assertTrue(passwordEncoder.matches(signUpFormDto.getPassword(), savedAccount.getPassword()));
    }

    @Test
    @DisplayName("로그인 테스트")
    public void login() {
        // given
        SignUpFormDto signUpFormDto = getSignUpFormDto();
        accountService.saveNewAccount(signUpFormDto);

        // when
        accountService.login(signUpFormDto.getUserIdentifier(), signUpFormDto.getPassword());

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(authentication.getName(), signUpFormDto.getUserIdentifier());
    }

    @Test
    @DisplayName("프로필 수정 테스트")
    @WithAccount("test1234")
    public void sendSignUpConfirmEmail() throws Exception {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        ProfileFormDto profileFormDto = getProfileFormDto();

        // when
        accountService.updateProfile(account, profileFormDto);

        // then
        assertEquals(account.getUserIdentifier(), profileFormDto.getUserIdentifier());
        assertEquals(account.getEmail(), profileFormDto.getEmail());
        assertEquals(account.getName(), profileFormDto.getName());
        assertEquals(account.getAddress1(), profileFormDto.getAddress1());
        assertEquals(account.getAddress2(), profileFormDto.getAddress2());
        assertEquals(account.getAddress3(), profileFormDto.getAddress3());
        assertTrue(passwordEncoder.matches(profileFormDto.getPassword(), account.getPassword()));
    }

    @Test
    @DisplayName("아이디 찾기 테스트")
    @WithAccount("test1234")
    public void findId() throws Exception {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        String email = account.getEmail();
        String name = account.getName();

        // when
        String findId = accountService.findId(email, name);

        // then
        assertEquals(account.getUserIdentifier(), findId);
    }

    @Test
    @DisplayName("비밀번호 찾기 테스트")
    @WithAccount("test1234")
    public void findPassword() throws Exception {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        String email = account.getEmail();
        String name = account.getName();

        // when
        accountService.sendEmailAndUpdatePassword(email, name);

        // then
        assertFalse(passwordEncoder.matches("test1234", account.getPassword()));
    }

    @Test
    @DisplayName("이메일 중복 검사")
    @WithAccount("test1234")
    public void userEmailCheck() throws Exception {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        String email = account.getEmail();
        String name = account.getName();

        // when
        boolean result = accountService.userEmailCheck(email, name);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("아이디 중복 검사")
    @WithAccount("test1234")
    public void checkId() throws Exception {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        String userIdentifier = account.getUserIdentifier();

        // when
        boolean result = accountService.checkId(userIdentifier);

        // then
        assertEquals(result, false);
    }

    private static ProfileFormDto getProfileFormDto() {
        ProfileFormDto profileFormDto = new ProfileFormDto();
        profileFormDto.setUserIdentifier("수정");
        profileFormDto.setEmail("change@naver.com");
        profileFormDto.setPassword("change1234");
        profileFormDto.setPasswordConfirm("change1234");
        profileFormDto.setAddress1("test1");
        profileFormDto.setAddress2("test2");
        profileFormDto.setAddress3("test3");
        return profileFormDto;
    }

    private static SignUpFormDto getSignUpFormDto() {
        SignUpFormDto signUpFormDto = SignUpFormDto.builder()
                .userIdentifier("test1234")
                .email("test@test.com")
                .name("test")
                .password("test1234")
                .passwordConfirm("test1234")
                .address1("test")
                .address2("test")
                .address3("test")
                .build();
        return signUpFormDto;
    }
}