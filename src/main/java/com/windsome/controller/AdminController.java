package com.windsome.controller;

import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.ItemSearchDto;
import com.windsome.entity.Item;
import com.windsome.service.CategoryService;
import com.windsome.service.ItemService;
import com.windsome.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ItemService itemService;

    @GetMapping("/main")
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

//    @PostMapping("/order/{orderId}/cancel")
//    public ResponseEntity<Object> cancelOrder(@PathVariable("orderId") Long orderId, @CurrentAccount Account account) {
//        orderService.cancelOrder(orderId);
//        return ResponseEntity.ok().body(orderId);
//    }
}

