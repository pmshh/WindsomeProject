package com.windsome.controller;

import com.windsome.dto.ItemDto;
import com.windsome.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ItemService itemService;

    @GetMapping("main")
    public String home() {
        return "admin/main";
    }

    @GetMapping("/item/new")
    public String itemForm(Model model) {
        model.addAttribute(new ItemDto());
        return "admin/item-form";
    }

    @PostMapping("/item/new")
    public String item(ItemDto itemDto, Model model) {
        itemService.itemSave(itemDto);
        return "redirect:/";
    }
}
