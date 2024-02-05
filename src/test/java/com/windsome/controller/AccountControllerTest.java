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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
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
                        .param("userIdentifier", "test1234")
                        .param("email", "email..")
                        .param("name", "gildong")
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
                        .param("userIdentifier", "test1234")
                        .param("email", "email@email.com")
                        .param("name", "gildong")
                        .param("password", "test1234")
                        .param("passwordConfirm", "test1234")
                        .param("address1", "test")
                        .param("address2", "test")
                        .param("address3", "test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByUserIdentifier("test1234");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "test1234");
    }

    @WithAccount("test1234")
    @DisplayName("프로필 수정 화면 보이는지 테스트")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/account/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profileFormDto"));
    }

    @WithAccount("test1234")
    @DisplayName("프로필 수정 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        Account account = accountRepository.findByUserIdentifier("test1234");
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

    @WithAccount("test1234")
    @DisplayName("프로필 수정 - 입력값 에러")
    @Test
    void updateProfile_with_error() throws Exception {
        Account account = accountRepository.findByUserIdentifier("test1234");
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

    @WithAccount("test1234")
    @DisplayName("이메일 중복 체크 후 이메일 인증 - 성공")
    @Test
    void confirmEmail() throws Exception {
        mockMvc.perform(get("/check/email")
                        .param("email", "newemail@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")));
    }

    @WithAccount("test1234")
    @DisplayName("이메일 중복 체크 후 이메일 인증 - 실패")
    @Test
    void confirmEmailFail() throws Exception {
        Account account = accountRepository.findByUserIdentifier("test1234");
        mockMvc.perform(get("/check/email")
                        .param("email", account.getEmail()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 사용중인 이메일입니다."));
    }

    @WithAccount("test1234")
    @DisplayName("아이디 중복 체크 - 성공")
    @Test
    void duplicateCheckEmail() throws Exception {
        mockMvc.perform(post("/check/id")
                        .param("userId", "test12345678")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("사용 가능한 아이디입니다."));
    }

    @WithAccount("test1234")
    @DisplayName("아이디 중복 체크 - 실패")
    @Test
    void duplicateCheckEmailFail() throws Exception {
        Account account = accountRepository.findByUserIdentifier("test1234");
        mockMvc.perform(post("/check/id")
                        .param("userId", account.getUserIdentifier())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 사용중인 아이디입니다."));
    }

    @WithAccount("test1234")
    @DisplayName("아이디/비밀번호 찾기 화면 보이는지 테스트")
    @Test
    void findAccount() throws Exception {
        mockMvc.perform(get("/find/account")
                        .param("type", "findId"))
                .andExpect(model().attributeExists("type"))
                .andExpect(view().name("account/findAccount"));
    }

    @WithAccount("test1234")
    @DisplayName("아이디 찾기 화면 (쿼리 파라미터 숨기기 위한 용도) 보이는지 테스트")
    @Test
    void findIdRedirect() throws Exception {
        mockMvc.perform(get("/find/id.do")
                        .param("name", "test")
                        .param("email", "test@naver.com"))
                .andExpect(redirectedUrl("/find/id"));
    }

    @WithAccount("test1234")
    @DisplayName("아이디 찾기 화면 보이는지 테스트")
    @Test
    void findId() throws Exception {
        mockMvc.perform(get("/find/id"))
                .andExpect(view().name("account/findId"));
    }

    @WithAccount("test1234")
    @DisplayName("비밀번호 변경 화면 (쿼리 파라미터 숨기기 위한 용도) 보이는지 테스트")
    @Test
    void updatePwRedirect() throws Exception {
        mockMvc.perform(get("/update/pw.do")
                        .param("userIdentifier", "test")
                        .param("name", "test")
                        .param("email", "test@naver.com"))
                .andExpect(redirectedUrl("/update/pw"));
    }

    @WithAccount("test1234")
    @DisplayName("비밀번호 변경 화면 보이는지 테스트")
    @Test
    void updatePwForm() throws Exception {
        mockMvc.perform(get("/update/pw"))
                .andExpect(view().name("account/updatePw"));
    }

    @WithAccount("test1234")
    @DisplayName("비밀번호 변경 테스트")
    @Test
    void updatePw() throws Exception {
        mockMvc.perform(post("/update/pw")
                        .param("userIdentifier", "test1234")
                        .param("password", "testtest1234@")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("비밀번호가 변경되었습니다."));
    }

    @WithAccount("test1234")
    @DisplayName("아이디/비밀번호 찾기 화면 - 이메일 인증 테스트")
    @Test
    void findIdPwEmailConfirm() throws Exception {
        mockMvc.perform(post("/find/sendEmail")
                        .param("email", "test1234@email.com")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @WithAccount("test1234")
    @DisplayName("마이 페이지 화면 보이는지 테스트")
    @Test
    void mypage() throws Exception {
        mockMvc.perform(get("/mypage"))
                .andExpect(model().attributeExists("cartItemTotalCount"))
                .andExpect(model().attributeExists("myPageInfo"))
                .andExpect(model().attributeExists("userOrderCount"))
                .andExpect(view().name("account/mypage"));
    }
}