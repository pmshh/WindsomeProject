package com.windsome.controller.admin;

import com.windsome.dto.order.OrderMngDto;
import com.windsome.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@MockBean(JpaMetamodelMappingContext.class)
class AdminOrderControllerTest {

    @MockBean OrderService orderService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("주문 조회 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getOrderListTest() throws Exception {
        // Mocking
        Page<OrderMngDto> orders = new PageImpl<>(Collections.emptyList()); // 빈 페이지 생성
        given(orderService.getAdminPageOrderList(anyString(), any())).willReturn(orders);

        // Perform & Verify
        mockMvc.perform(get("/admin/orders").param("userIdentifier", "user123").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 취소 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void cancelOrderTest() throws Exception {
        // Mocking
        doNothing().when(orderService).cancelOrder(anyLong());

        // Perform & Verify
        mockMvc.perform(post("/admin/order/{orderId}/cancel", 123L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("주문이 취소되었습니다.".getBytes(StandardCharsets.UTF_8)));
    }
}