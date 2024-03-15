package com.windsome.controller;

import com.windsome.config.security.CurrentMember;
import com.windsome.dto.order.*;
import com.windsome.entity.member.Member;
import com.windsome.repository.member.MemberRepository;
import com.windsome.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberRepository memberRepository;

    /**
     * 주문 조회
     */
    @GetMapping("/orders")
    public String showOrderList(Optional<Integer> page, @CurrentMember Member member, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 5);
        model.addAttribute("orders", orderService.getOrderList(member.getUserIdentifier(), pageable));
        model.addAttribute("maxPage", 5);
        return "order/order-history";
    }

    /**
     * 상품 주문 화면
     */
    @GetMapping("/orders/new")
    public String createOrderForm(OrderProductListDTO orderProductListDTO, @CurrentMember Member member, Model model) {
        model.addAttribute("orderProducts", orderService.getOrderProductsInfo(orderProductListDTO));
        model.addAttribute("member", memberRepository.findById(member.getId()).orElseThrow(EntityNotFoundException::new));
        return "order/order-form";
    }

    /**
     * 상품 주문
     */
    @PostMapping("/orders/new")
    public String createOrder(OrderRequestDTO orderRequestDTO, @CurrentMember Member member, RedirectAttributes redirectAttributes) {
        try {
            orderService.order(orderRequestDTO, member.getUserIdentifier());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "상품 주문 도중 오류가 발생하였습니다.");
            return "redirect:/orders";
        }
        redirectAttributes.addFlashAttribute("message", "상품이 주문되었습니다.");
        return "redirect:/orders";
    }

    /**
     * 주문 취소
     */
    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId, @CurrentMember Member member) {
        if (orderService.verifyOrderCancellationPermission(orderId, member.getUserIdentifier())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("주문 취소 권한이 없습니다.");
        }

        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().body("주문이 취소되었습니다.");
    }

    /**
     * 주문 상세
     */
    @GetMapping("/orders/{orderId}")
    public String showOrderDetail(@PathVariable(value = "orderId") Long orderId, Model model) {
        OrderDetailDTO orderDetail = orderService.getOrderDetail(orderId);
        model.addAttribute("orderDetail", orderDetail);
        return "order/order-detail";
    }
}
