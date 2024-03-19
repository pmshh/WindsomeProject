package com.windsome.controller;

import com.windsome.dto.member.SignUpRequestDTO;
import com.windsome.repository.member.MemberRepository;
import com.windsome.service.member.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class MainControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @BeforeEach
    void beforeEach() {
        SignUpRequestDTO signUpRequestDTO = SignUpRequestDTO.builder()
                .userIdentifier("test1234")
                .email("test1234@email.com")
                .name("gildong")
                .password("test1234")
                .zipcode("test")
                .addr("test")
                .addrDetail("test")
                .build();
        memberService.createAccount(signUpRequestDTO);
    }

    @AfterEach
    void afterEach() {
        memberRepository.deleteAll();
    }

    @DisplayName("로그인 성공 - 입력값 정상")
    @Test
    void login() throws Exception {
        mockMvc.perform(post("/login")
                        .param("userId", "test1234")
                        .param("password", "test1234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test1234"));
    }

    @DisplayName("로그인 실패 - 입력값 오류")
    @Test
    void login_fail() throws Exception {
        String urlEncode = URLEncoder.encode("아이디 또는 비밀번호를 잘못 입력했습니다.<br>입력하신 내용을 다시 확인해주세요.", StandardCharsets.UTF_8);

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