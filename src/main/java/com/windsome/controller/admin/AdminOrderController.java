package com.windsome.controller.admin;

import com.windsome.constant.OrderProductStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.dto.order.AdminPageOrderDTO;
import com.windsome.repository.product.InventoryRepository;
import com.windsome.service.AdminService;
import com.windsome.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminOrderController {

    private final OrderService orderService;
    private final AdminService adminService;
    private final InventoryRepository inventoryRepository;

    /**
     * 주문 관리 - 조회
     */
    @GetMapping("/orders")
    public String getOrderList(@RequestParam(name = "userIdentifier", required = false, defaultValue = "") String userIdentifier,
                               @RequestParam(name = "page", required = false, defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        model.addAttribute("orders", adminService.getOrderList(userIdentifier, pageable));
        model.addAttribute("maxPage", 10);
        return "admin/order/order-management";
    }

    /**
     * 주문 상세
     */
    @GetMapping("/orders/{orderId}")
    public String showOrderDetail(@PathVariable("orderId") Long orderId, Model model) {
        model.addAttribute("orderDetail", orderService.getOrderDetail(orderId));
        model.addAttribute("orderId", orderId);
        return "admin/order/order-detail";
    }

    /**
     * 주문 수정 화면
     */
    @GetMapping("/orders/{orderId}/edit")
    public String updateOrderForm(@PathVariable("orderId") Long orderId, Model model) {
        model.addAttribute("orderDetail", orderService.getOrderDetail(orderId));
        model.addAttribute("orderProductStatus", OrderProductStatus.values());
        model.addAttribute("orderStatus", OrderStatus.values());
        model.addAttribute("orderId", orderId);
        return "admin/order/order-form";
    }

    /**
     * 주문 수정
     */
    @PostMapping("/orders/{orderId}")
    public String updateOrder(AdminPageOrderDTO adminPageOrderDTO, @PathVariable("orderId") Long orderId, RedirectAttributes redirectAttr) {
        adminService.updateOrder(orderId, adminPageOrderDTO);
        redirectAttr.addFlashAttribute("message", "주문이 수정되었습니다.");
        return "redirect:/admin/orders/" + orderId;
    }

    /**
     * 주문 관리 - 주문 취소
     */
    @PostMapping("/orders/cancel")
    public ResponseEntity<String> cancelOrder(@RequestBody Long[] orderIds) {
        try {
            adminService.cancelOrders(orderIds);
            return ResponseEntity.ok().body("주문이 취소되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("주문 취소 도중 오류가 발생하였습니다.");
        }
    }

    @GetMapping("/orders/inventories/{productId}")
    public ResponseEntity<Object> getInventories(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok().body(inventoryRepository.getInventoriesByProductId(productId));
    }
}
