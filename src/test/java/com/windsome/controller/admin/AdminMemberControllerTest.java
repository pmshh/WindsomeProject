package com.windsome.controller.admin;

import com.windsome.WithAccount;
import com.windsome.constant.Role;
import com.windsome.dto.member.AdminMemberDetailDTO;
import com.windsome.dto.member.MemberListSearchDTO;
import com.windsome.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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

//@WebMvcTest(AdminMemberController.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@MockBean(JpaMetamodelMappingContext.class)
@Transactional
class AdminMemberControllerTest {

    @MockBean AdminService adminService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("회원 목록 조회 테스트")
    @WithAccount("admin1234")
    void getMembersTest() throws Exception {
        // Mocking
        given(adminService.getMemberListForAdminPage(any(MemberListSearchDTO.class), any())).willReturn(new PageImpl<>(Collections.emptyList()));

        // Perform & Verify
        mockMvc.perform(get("/admin/members")
                        .param("searchStateType", "")
                        .param("searchType", "")
                        .param("searchQuery","")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/member/member-management"))
                .andExpect(model().attributeExists("memberListSearchDto"))
                .andExpect(model().attributeExists("memberInfoDto"))
                .andExpect(model().attribute("maxPage", 10));
    }

    @Test
    @DisplayName("회원 상세 조회 테스트")
    @WithAccount("admin1234")
    void getMemberByIdTest() throws Exception {
        // Mocking
        AdminMemberDetailDTO testDto = AdminMemberDetailDTO.builder()
                .id(1L)
                .userIdentifier("testUser")
                .password("testPassword")
                .name("Test User")
                .email("test@example.com")
                .zipcode("123 Test St")
                .addr("Apt 101")
                .addrDetail("Test City")
                .availablePoints(100)
                .totalUsedPoints(1000)
                .totalEarnedPoints(100)
                .build();
        given(adminService.getMemberDetails(anyLong())).willReturn(testDto);

        // Perform & Verify
        mockMvc.perform(get("/admin/members/{id}", 123L))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/member/member-form"))
                .andExpect(model().attribute("viewName", "dtlPage"))
                .andExpect(model().attribute("memberDetails", testDto));
    }

    @Test
    @DisplayName("회원 수정 화면 테스트")
    @WithAccount("admin1234")
    void showMemberEditFormTest() throws Exception {
        // Given
        AdminMemberDetailDTO testDto = AdminMemberDetailDTO.builder()
                .id(1L)
                .userIdentifier("testUser")
                .password("testPassword")
                .name("Test User")
                .email("test@example.com")
                .zipcode("123 Test St")
                .addr("Apt 101")
                .addrDetail("Test City")
                .availablePoints(100)
                .totalUsedPoints(1000)
                .totalEarnedPoints(100)
                .build();
        given(adminService.getMemberDetails(1L)).willReturn(testDto);

        // Perform & Verify
        mockMvc.perform(get("/admin/members/1/edit").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/member/member-form"))
                .andExpect(model().attribute("viewName", "updatePage"))
                .andExpect(model().attributeExists("memberDetails"));
    }

    @Test
    @DisplayName("회원 정보 수정 테스트")
    @WithAccount("admin1234")
    void updateMemberTest() throws Exception {
        // Given
        AdminMemberDetailDTO testDto = AdminMemberDetailDTO.builder()
                .id(1L)
                .userIdentifier("testUser")
                .password("testPassword")
                .name("Test User")
                .email("test@example.com")
                .zipcode("123 Test St")
                .addr("Apt 101")
                .addrDetail("Test City")
                .availablePoints(100)
                .totalUsedPoints(1000)
                .totalEarnedPoints(100)
                .build();
        doNothing().when(adminService).updateMember(testDto);

        // Perform & Verify
        mockMvc.perform(post("/admin/members/1")
                        .param("id", "1")
                        .param("userIdentifier", "testUser")
                        .param("password", "testPassword")
                        .param("name", "Test User")
                        .param("email", "test@example.com")
                        .param("zipcode", "123 Test St")
                        .param("addr", "Apt 101")
                        .param("addrDetail", "Test City")
                        .param("availablePoints", "100")
                        .param("totalUsedPoints", "1000")
                        .param("totalEarnedPoints", "100")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/members"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("회원 권한 수정 테스트")
    @WithAccount("admin1234")
    void updateMemberRoleTest() throws Exception {
        // Given
        doNothing().when(adminService).updateMemberRole(anyLong(), any(Role.class));

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
    @WithAccount("admin1234")
    void deleteMemberTest() throws Exception {
        // Given
        doNothing().when(adminService).deleteMembers(any());

        // Perform & Verify
        mockMvc.perform(delete("/admin/members/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("사용자가 삭제되었습니다."));
    }
}