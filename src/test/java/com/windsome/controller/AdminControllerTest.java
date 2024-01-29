package com.windsome.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AdminControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("상품 등록 페이지 - 일반 회원 접근")
    @WithMockUser(username = "user", roles = "USER")
    public void itemFormUserTest() throws Exception {
        mockMvc.perform(get("/admin/item"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상품 등록 페이지 - 관리자 접근")
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void itemFormAdminTest() throws Exception {
        mockMvc.perform(get("/admin/item"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}