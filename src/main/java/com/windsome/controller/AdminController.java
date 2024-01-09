package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.constant.ItemSellStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.dto.ItemSearchDto;
import com.windsome.dto.OrderMngDto;
import com.windsome.entity.Account;
import com.windsome.entity.Item;
import com.windsome.service.ItemService;
import com.windsome.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final ItemService itemService;
    private final OrderService orderService;

    @GetMapping("/admin/main")
    public String home() {
        return "admin/main";
    }

    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        model.addAttribute("sellStatus", ItemSellStatus.SELL);
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 10);
        return "admin/item/itemMng";
    }

    @GetMapping(value = {"/admin/orders", "/admin/orders/{page}"})
    public String orderManage(String userIdentifier, @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        Page<OrderMngDto> orders = orderService.getAdminPageOrderList(userIdentifier == null ? "" : userIdentifier, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("maxPage", 10);
        model.addAttribute("orderStatus", OrderStatus.READY);
        return "admin/order/orderMng";
    }

    @PostMapping("/admin/order/{orderId}/cancel")
    public ResponseEntity<Object> cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().body(orderId);
    }
}

