package com.windsome.controller;

import com.windsome.WithAccount;
import com.windsome.entity.Account;
import com.windsome.repository.AccountRepository;
import com.windsome.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/signUp"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/signUp"))
                .andExpect(model().attributeExists("signUpFormDto"));
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/signUp")
                        .param("userIdentifier", "pms000723")
                        .param("email", "email..")
                        .param("name", "minsu")
                        .param("password", "1234")
                        .param("passwordConfirm", "test1234")
                        .param("address1", "test")
                        .param("address2", "test")
                        .param("address3", "test")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/signUp"));
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit() throws Exception {
        mockMvc.perform(post("/signUp")
                        .param("userIdentifier", "pms000723")
                        .param("email", "email@email.com")
                        .param("name", "minsu")
                        .param("password", "test1234")
                        .param("passwordConfirm", "test1234")
                        .param("address1", "test")
                        .param("address2", "test")
                        .param("address3", "test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByUserIdentifier("pms000723");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "test1234");
    }

    @WithAccount("pms000723")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/account/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileFormDto"));
    }

    @WithAccount("pms000723")
    @DisplayName("프로필 수정 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        Account account = accountRepository.findByUserIdentifier("pms000723");
        mockMvc.perform(post("/account/profile")
                        .param("userIdentifier", account.getUserIdentifier())
                        .param("password", "change1234")
                        .param("passwordConfirm", "change1234")
                        .param("name", "이순신")
                        .param("email", account.getEmail())
                        .param("address1", account.getAddress1())
                        .param("address2", account.getAddress2())
                        .param("address3", account.getAddress3())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/profile"))
                .andExpect(flash().attributeExists("message"));

        assertEquals("이순신", account.getName());
        assertTrue(passwordEncoder.matches("change1234", account.getPassword()));
    }

    @WithAccount("pms000723")
    @DisplayName("프로필 수정 - 입력값 에러")
    @Test
    void updateProfile_with_error() throws Exception {
        Account account = accountRepository.findByUserIdentifier("pms000723");
        mockMvc.perform(post("/account/profile")
                        .param("userIdentifier", account.getUserIdentifier())
                        .param("password", "changePassword123")
                        .param("passwordConfirm", "changePassword123")
                        .param("name", "닉네임을 길게 입력해서 에러를 발생시키자")
                        .param("email", account.getEmail())
                        .param("address1", account.getAddress1())
                        .param("address2", account.getAddress2())
                        .param("address3", account.getAddress3())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileFormDto"))
                .andExpect(model().hasErrors());

        assertNotEquals("닉네임을 길게 입력해서 에러를 발생시키자", account.getName());
        assertFalse(passwordEncoder.matches("changePassword123", account.getPassword()));
    }
}