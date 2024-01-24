package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.ItemFormDto;
import com.windsome.entity.Account;
import com.windsome.service.CartService;
import com.windsome.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    /**
     * 상품 상세 화면
     */
    @GetMapping("/item/{itemId}")
    public String itemDetail(@CurrentAccount Account account, Model model, @PathVariable("itemId") Long itemId) {
        ItemFormDto itemFormDto = itemService.getItemFormDto(itemId);
        Long cartItemTotalCount = null;
        if (account != null) {
            cartItemTotalCount = cartService.getCartItemTotalCount(account);
        }
        model.addAttribute("cartItemTotalCount", cartItemTotalCount);
        model.addAttribute("item", itemFormDto);
        return "main/item/itemDtl";
    }
}
