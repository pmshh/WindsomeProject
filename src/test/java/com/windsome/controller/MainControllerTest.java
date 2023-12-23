package com.windsome.controller;

import com.windsome.repository.AccountRepository;
import com.windsome.service.AccountService;
import com.windsome.dto.SignUpDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    void beforeEach() {
        SignUpDto signUpDto = new SignUpDto();
        signUpDto.setUserIdentifier("pms000723");
        signUpDto.setEmail("pms000723@gmail.com");
        signUpDto.setName("홍길동");
        signUpDto.setPassword("12345678");
        signUpDto.setAddress1("test");
        signUpDto.setAddress2("test");
        signUpDto.setAddress3("test");
        accountService.processNewAccount(signUpDto);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @DisplayName("로그인 성공 - 입력값 정상")
    @Test
    void login() throws Exception {
        mockMvc.perform(post("/login")
                        .param("userId", "pms000723")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("pms000723"));
    }

    @DisplayName("로그인 실패 - 입력값 오류")
    @Test
    void login_fail() throws Exception {
        String urlEncode = URLEncoder.encode("아이디 또는 비밀번호가 맞지 않습니다. 다시 확인해 주세요.", StandardCharsets.UTF_8);

        mockMvc.perform(post("/login")
                        .param("userId", "test1234@gmail.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true&exception=" + urlEncode))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }


}