package com.windsome.controller.admin;

import com.windsome.service.AdminService;
import com.windsome.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminOrderController {

    private final OrderService orderService;
    private final AdminService adminService;

    /**
     * 주문 관리 - 조회
     */
    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String getOrderList(@RequestParam(name = "userIdentifier", required = false, defaultValue = "") String userIdentifier,
                               @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        model.addAttribute("orders", adminService.getOrderList(userIdentifier, pageable));
        model.addAttribute("maxPage", 10);
        return "admin/order/order-management";
    }

    /**
     * 주문 관리 - 주문 취소
     */
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok().body("주문이 취소되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("주문 취소 도중 오류가 발생하였습니다.");
        }
    }
}
