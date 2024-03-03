//package com.windsome.controller;
//
//import com.windsome.WithAccount;
//import com.windsome.constant.ProductSellStatus;
//import com.windsome.dto.order.OrderRequestDTO;
//import com.windsome.dto.order.OrderProductRequestDTO;
//import com.windsome.entity.Product;
//import com.windsome.repository.member.MemberRepository;
//import com.windsome.repository.order.OrderRepository;
//import com.windsome.repository.orderProduct.OrderProductRepository;
//import com.windsome.repository.product.ProductRepository;
//import com.windsome.service.OrderService;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
//class OrderControllerTest {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired ProductRepository productRepository;
//    @Autowired OrderService orderService;
//    @Autowired OrderRepository orderRepository;
//    @Autowired OrderProductRepository orderProductRepository;
//    @Autowired MemberRepository memberRepository;
//
//    @AfterEach
//    void afterEach() {
//        orderProductRepository.deleteAll();
//        orderRepository.deleteAll();
//        productRepository.deleteAll();
//        memberRepository.deleteAll();
//    }
//
//    @WithAccount("test1234")
//    @DisplayName("주문 조회 화면 보이는지 테스트")
//    @Test
//    void orderHist() throws Exception {
//        mockMvc.perform(get("/orders"))
//                .andExpect(model().attributeExists("orders"))
//                .andExpect(model().attributeExists("maxPage"))
//                .andExpect(view().name("order/order-history"));
//    }
//
//    @WithAccount("test1234")
//    @DisplayName("주문 취소 테스트")
//    @Test
//    void cancelOrder() throws Exception {
//        Product product = saveProduct();
//
//        OrderRequestDTO orderDto = getOrderDto(product);
//        Long orderId = orderService.order(orderDto, "test1234");
//
//        mockMvc.perform(patch("/orders/" + orderId + "/cancel")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("text/plain;charset=UTF-8"))
//                .andExpect(content().string("주문이 최소되었습니다."));
//    }
//
//    public Product saveProduct() {
//        Product product = Product.builder()
//                .name("테스트 상품")
//                .price(10000)
//                .productDetail("테스트 상품 상세 설명")
//                .productSellStatus(ProductSellStatus.SELL)
//                .stockNumber(100)
//                .build();
//        return productRepository.save(product);
//    }
//
//    private static OrderRequestDTO getOrderDto(Product product) {
//        List<OrderProductRequestDTO> orderItemDtoList = new ArrayList<>();
//        OrderProductRequestDTO orderItemDto = new OrderProductRequestDTO();
//        orderItemDto.setId(product.getId());
//        orderItemDto.setPrice(product.getPrice());
//        orderItemDto.setCount(10);
//        orderItemDtoList.add(orderItemDto);
//
//        OrderRequestDTO orderDto = new OrderRequestDTO("test", "test", "test", "test", "test", "test", orderItemDtoList, 0, 0, 10000, 500, 10000);
//        return orderDto;
//    }
//}