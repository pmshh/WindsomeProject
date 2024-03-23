package com.windsome.controller;

import com.windsome.advice.MemberControllerAdvice;
import com.windsome.controller.order.OrderController;
import com.windsome.dto.member.MemberDetailDTO;
import com.windsome.dto.order.*;
import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import com.windsome.repository.member.MemberRepository;
import com.windsome.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
@MockBean(JpaMetamodelMappingContext.class)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
public class OrderControllerTest {

    @Autowired MockMvc mockMvc;

    @Autowired private OrderController orderController;

    @MockBean private OrderService orderService;
    @MockBean private MemberRepository memberRepository;
    @MockBean MemberControllerAdvice memberControllerAdvice;

    @Test
    void testShowOrderList() {
        // Given
        Model mockModel = mock(Model.class);
        Member member = mock(Member.class);
        member.setUserIdentifier("test1234");
        Pageable pageable = PageRequest.of(1, 5);
        Page<OrderHistResponseDTO> page = mock(Page.class);

        when(orderService.getOrderList(member.getUserIdentifier(), pageable)).thenReturn(page);

        // When
        String result = orderController.showOrderList(Optional.of(1), member, mockModel);

        // Then
        assertEquals("order/order-history", result);
        verify(mockModel).addAttribute(eq("orders"), eq(page));
        verify(mockModel).addAttribute(eq("maxPage"), eq(5));
    }

    @Test
    void testCreateOrderForm() {
        // Given
        Model mockModel = mock(Model.class);

        Member currentAccount = new Member();
        currentAccount.setId(1L);

        OrderProductListDTO orderProductListDTO = mock(OrderProductListDTO.class);

        List<OrderProductResponseDTO> orderProductResponseDTOList = new ArrayList<>();

        MemberDetailDTO memberDetailDTO = mock(MemberDetailDTO.class);

        Address address = mock(Address.class);
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);

        when(orderService.getOrderProductsInfo(orderProductListDTO)).thenReturn(orderProductResponseDTOList);
        when(orderService.getMemberDetail(currentAccount.getId())).thenReturn(memberDetailDTO);
        when(orderService.getDefaultShippingAddress(currentAccount.getId())).thenReturn(address);
        when(orderService.getAddressList(currentAccount.getId())).thenReturn(addressList);

        // When
        String result = orderController.createOrderForm(orderProductListDTO, currentAccount, mockModel);

        // Then
        assertEquals("order/order-form", result);
        verify(mockModel).addAttribute(eq("orderProducts"), eq(orderProductResponseDTOList));
        verify(mockModel).addAttribute(eq("member"), eq(memberDetailDTO));
        verify(mockModel).addAttribute(eq("defaultAddress"), eq(address));
        verify(mockModel).addAttribute(eq("addressList"), eq(addressList));
    }

    @Test
    void testCreateOrder() {
        // Given
        OrderRequestDTO orderRequestDTO = mock(OrderRequestDTO.class);
        Member member = mock(Member.class);
        RedirectAttributes mockRedirectAttributes = mock(RedirectAttributes.class);
        member.setUserIdentifier("test1234");

        when(orderService.order(orderRequestDTO, member.getId())).thenReturn(1L);

        // When
        String result = orderController.createOrder(orderRequestDTO, member, mockRedirectAttributes);

        // Then
        assertEquals("redirect:/orders", result);
        verify(mockRedirectAttributes).addFlashAttribute(eq("message"), eq("상품이 주문되었습니다."));
    }

    @Test
    void testCancelOrder() {
        // Given
        Long orderId = 123L;
        Member member = mock(Member.class);
        member.setUserIdentifier("test1234");

        when(orderService.verifyOrderCancellationPermission(orderId, member.getId())).thenReturn(false);

        // When
        ResponseEntity<String> responseEntity = orderController.cancelOrder(orderId, member);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("주문이 취소되었습니다.", responseEntity.getBody());
    }

    @Test
    void testShowOrderDetail() {
        // Given
        Long orderId = 123L;
        Model mockModel = mock(Model.class);
        OrderDetailDTO orderDetailDTO = new OrderDetailDTO();

        // Stubbing getOrderDetail() method to return a mock order detail DTO
        when(orderService.getOrderDetail(orderId)).thenReturn(orderDetailDTO);

        // When
        String viewName = orderController.showOrderDetail(orderId, mockModel);

        // Then
        assertEquals("order/order-detail", viewName);
        verify(mockModel).addAttribute(eq("orderDetail"), eq(orderDetailDTO));
    }
}