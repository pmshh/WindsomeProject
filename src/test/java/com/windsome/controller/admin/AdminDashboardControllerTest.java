package com.windsome.controller.admin;

import com.windsome.WithAccount;
import com.windsome.dto.admin.DashboardInfoDto;
import com.windsome.dto.admin.OrderManagementDTO;
import com.windsome.dto.board.qa.QaListDto;
import com.windsome.dto.board.qa.QaSearchDto;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.entity.Product;
import com.windsome.service.*;
import com.windsome.service.board.QaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(AdminDashboardController.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@MockBean(JpaMetamodelMappingContext.class)
@Transactional
class AdminDashboardControllerTest {

    @MockBean AdminService adminService;
    @MockBean
    ProductService productService;
    @MockBean OrderService orderService;
    @MockBean QaService qaService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("dashboard 화면 잘 보이는지 테스트")
    @WithAccount("admin1234")
    void getDashboard() throws Exception {
        DashboardInfoDto dashboardInfoDto = new DashboardInfoDto();
        given(adminService.getDashboardData()).willReturn(dashboardInfoDto);

        Page<Product> products = new PageImpl<>(Collections.emptyList());
        given(adminService.getProductList(any(ProductSearchDTO.class), any(Pageable.class))).willReturn(products);

        Page<OrderManagementDTO> orders = new PageImpl<>(Collections.emptyList());
        given(adminService.getOrderList(anyString(), any(Pageable.class))).willReturn(orders);

        Page<QaListDto> qaList = new PageImpl<>(Collections.emptyList());
        given(qaService.getQaList(any(QaSearchDto.class), any(Pageable.class))).willReturn(qaList);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(model().attributeExists("dashboardData"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("qaList"))
                .andExpect(view().name("admin/dashboard"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
