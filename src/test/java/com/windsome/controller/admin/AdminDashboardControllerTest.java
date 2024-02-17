package com.windsome.controller.admin;

import com.windsome.dto.admin.DashboardDataDto;
import com.windsome.dto.board.qa.QaListDto;
import com.windsome.dto.board.qa.QaSearchDto;
import com.windsome.dto.item.ItemSearchDto;
import com.windsome.dto.order.OrderMngDto;
import com.windsome.entity.Item;
import com.windsome.service.*;
import com.windsome.service.board.QaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@MockBean(JpaMetamodelMappingContext.class)
class AdminDashboardControllerTest {

    @MockBean AdminService adminService;
    @MockBean ItemService itemService;
    @MockBean OrderService orderService;
    @MockBean QaService qaService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("dashboard 화면 잘 보이는지 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getDashboard() throws Exception {
        DashboardDataDto dashboardDataDto = new DashboardDataDto();
        given(adminService.getDashboardData()).willReturn(dashboardDataDto);

        Page<Item> items = new PageImpl<>(Collections.emptyList());
        given(itemService.getAdminItemPage(any(ItemSearchDto.class), any(Pageable.class))).willReturn(items);

        Page<OrderMngDto> orders = new PageImpl<>(Collections.emptyList());
        given(orderService.getAdminPageOrderList(anyString(), any(Pageable.class))).willReturn(orders);

        Page<QaListDto> qaList = new PageImpl<>(Collections.emptyList());
        given(qaService.getQaList(any(QaSearchDto.class), any(Pageable.class))).willReturn(qaList);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(model().attributeExists("dashboardData"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("qaList"))
                .andExpect(view().name("admin/dashboard"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
