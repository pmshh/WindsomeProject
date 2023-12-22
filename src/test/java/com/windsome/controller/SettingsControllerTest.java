package com.windsome.controller;

import com.windsome.WithAccount;
import com.windsome.repository.AccountRepository;
import com.windsome.service.AccountService;
import com.windsome.controller.SettingsController;
import com.windsome.entity.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("pms000723")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"));
    }

    @WithAccount("pms000723")
    @DisplayName("프로필 수정 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String changeNickname = "닉네임 변경 테스트";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("newPassword", "change1234")
                        .param("newPasswordConfirm", "change1234")
                        .param("name", changeNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByUserIdentifier("pms000723");
        assertEquals(changeNickname, account.getName());
        assertTrue(passwordEncoder.matches("change1234", account.getPassword()));
    }

    @WithAccount("pms000723")
    @DisplayName("프로필 수정 - 입력값 에러")
    @Test
    void updateProfile_with_error() throws Exception {
        String changeNickname = "닉네임을 길게 입력해서 에러를 발생시키자";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("newPassword", "change1234")
                        .param("newPasswordConfirm", "change1234")
                        .param("name", changeNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByUserIdentifier("pms000723");
        assertNotEquals(changeNickname, account.getName());
        assertFalse(passwordEncoder.matches("change1234", account.getPassword()));
    }


}