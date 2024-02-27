package com.windsome.controller.admin;

import com.windsome.constant.Role;
import com.windsome.dto.account.AccountSearchDto;
import com.windsome.dto.account.AdminPageProfileFormDto;
import com.windsome.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminMemberController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@MockBean(JpaMetamodelMappingContext.class)
class AdminMemberControllerTest {

    @MockBean AccountService accountService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("회원 목록 조회 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMembersTest() throws Exception {
        // Mocking
        given(accountService.getAccountInfo(any(AccountSearchDto.class), any())).willReturn(new PageImpl<>(Collections.emptyList()));

        // Perform & Verify
        mockMvc.perform(get("/admin/members")
                        .param("searchStateType", "")
                        .param("searchType", "")
                        .param("searchQuery","")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/account/accountMng"))
                .andExpect(model().attributeExists("accountSearchDto"))
                .andExpect(model().attributeExists("accountInfoDto"))
                .andExpect(model().attribute("maxPage", 10));
    }

    @Test
    @DisplayName("회원 조회 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getMemberByIdTest() throws Exception {
        // Mocking
        AdminPageProfileFormDto testDto = AdminPageProfileFormDto.builder()
                .id(1L)
                .userIdentifier("testUser")
                .password("testPassword")
                .name("Test User")
                .email("test@example.com")
                .address1("123 Test St")
                .address2("Apt 101")
                .address3("Test City")
                .point(100)
                .totalPoint(1000)
                .totalUsePoint(100)
                .totalOrderPrice(5000)
                .build();
        given(accountService.getAdminPageProfileFormDto(anyLong())).willReturn(testDto);

        // Perform & Verify
        mockMvc.perform(get("/admin/members/{id}", 123L))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/account/profileForm"))
                .andExpect(model().attribute("viewName", "dtlPage"))
                .andExpect(model().attribute("profileFormDto", testDto));
    }

    @Test
    @DisplayName("회원 수정 화면 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void showMemberEditFormTest() throws Exception {
        // Given
        AdminPageProfileFormDto testDto = AdminPageProfileFormDto.builder()
                .id(1L)
                .userIdentifier("testUser")
                .password("testPassword")
                .name("Test User")
                .email("test@example.com")
                .address1("123 Test St")
                .address2("Apt 101")
                .address3("Test City")
                .point(100)
                .totalPoint(1000)
                .totalUsePoint(100)
                .totalOrderPrice(5000)
                .build();
        given(accountService.getAdminPageProfileFormDto(1L)).willReturn(testDto);

        // Perform & Verify
        mockMvc.perform(get("/admin/members/1/edit").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/account/profileForm"))
                .andExpect(model().attribute("viewName", "updatePage"))
                .andExpect(model().attributeExists("profileFormDto"));
    }

    @Test
    @DisplayName("회원 정보 수정 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateMemberTest() throws Exception {
        // Given
        AdminPageProfileFormDto testDto = AdminPageProfileFormDto.builder()
                .id(1L)
                .userIdentifier("testUser")
                .password("testPassword")
                .name("Test User")
                .email("test@example.com")
                .address1("123 Test St")
                .address2("Apt 101")
                .address3("Test City")
                .point(100)
                .totalPoint(1000)
                .totalUsePoint(100)
                .totalOrderPrice(5000)
                .build();
        doNothing().when(accountService).updateProfileForAdmin(testDto);

        // Perform & Verify
        mockMvc.perform(post("/admin/members/1")
                        .param("id", "1")
                        .param("userIdentifier", "testUser")
                        .param("password", "testPassword")
                        .param("name", "Test User")
                        .param("email", "test@example.com")
                        .param("address1", "123 Test St")
                        .param("address2", "Apt 101")
                        .param("address3", "Test City")
                        .param("point", "100")
                        .param("totalPoint", "1000")
                        .param("totalUsePoint", "100")
                        .param("totalOrderPrice", "5000")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/members"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("회원 권한 수정 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateMemberRoleTest() throws Exception {
        // Given
        doNothing().when(accountService).updateAccountRole(anyLong(), any(Role.class));

        // Perform & Verify
        mockMvc.perform(patch("/admin/members/1")
                        .param("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("사용자의 권한이 수정되었습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteMemberTest() throws Exception {
        // Given
        doNothing().when(accountService).deleteAccount(anyLong());

        // Perform & Verify
        mockMvc.perform(delete("/admin/members/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("사용자가 삭제되었습니다."));
    }
}