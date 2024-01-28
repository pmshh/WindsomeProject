package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.constant.OrderStatus;
import com.windsome.dto.order.OrderDto;
import com.windsome.dto.order.OrderHistDto;
import com.windsome.dto.order.OrderPageDto;
import com.windsome.entity.Account;
import com.windsome.repository.AccountRepository;
import com.windsome.service.CartService;
import com.windsome.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final CartService cartService;
    private final AccountRepository accountRepository;

    /**
     * 주문 조회 화면
     */
    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, @CurrentAccount Account account, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 5);
        Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(account.getUserIdentifier(), pageable);

        model.addAttribute("cartItemTotalCount", cartService.getCartItemTotalCount(account));
        model.addAttribute("orders", ordersHistDtoList);
        model.addAttribute("maxPage", 5);
        model.addAttribute("orderStatus", OrderStatus.READY);
        return "order/orderHist";
    }

    /**
     * 주문서 작성 화면
     */
    @GetMapping("/order")
    public String orderForm(OrderPageDto orderPageDto, @CurrentAccount Account account, Model model) {
        model.addAttribute("cartItemTotalCount", cartService.getCartItemTotalCount(account));
        model.addAttribute("orders", orderService.getOrderItemsInfo(orderPageDto.getOrders()));
        model.addAttribute("account", accountRepository.findById(account.getId()).orElseThrow(EntityNotFoundException::new));
        return "order/orderForm";
    }

    /**
     * 상품 주문
     */
    @PostMapping("/order")
    public String order(OrderDto orderDto, @CurrentAccount Account account, RedirectAttributes redirectAttributes) {
        try {
            orderService.order(orderDto, account.getUserIdentifier());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("order_result", "order_fail");
            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("order_result", "order_ok");
        return "redirect:/";
    }

    /**
     * 주문 취소
     */
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<Object> cancelOrder(@PathVariable("orderId") Long orderId, @CurrentAccount Account account) {
//        if (!orderService.validateOrder(orderId, account.getUserIdentifier())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("주문 취소 권한이 없습니다.");
//        }

        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().body(orderId);
    }
}
