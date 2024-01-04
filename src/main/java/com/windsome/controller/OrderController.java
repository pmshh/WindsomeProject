package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.constant.ItemSellStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.dto.OrderDto;
import com.windsome.dto.OrderHistDto;
import com.windsome.entity.Account;
import com.windsome.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, @CurrentAccount Account account, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 5);

        Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(account.getUserIdentifier(), pageable);

        model.addAttribute("orders", ordersHistDtoList);
        model.addAttribute("maxPage", 5);
        model.addAttribute("orderStatus", OrderStatus.ORDER);
        return "order/orderHist";
    }

    @PostMapping("/order")
    public ResponseEntity<Object> order(@RequestBody @Valid OrderDto orderDto, BindingResult bindingResult, @CurrentAccount Account account) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(sb.toString());
        }

        Long orderId;
        try {
            orderId = orderService.order(orderDto, account.getUserIdentifier());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().body(orderId);
    }

    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<Object> cancelOrder(@PathVariable("orderId") Long orderId, @CurrentAccount Account account) {
        if (!orderService.validateOrder(orderId, account.getUserIdentifier())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("주문 취소 권한이 없습니다.");
        }

        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().body(orderId);
    }
}
