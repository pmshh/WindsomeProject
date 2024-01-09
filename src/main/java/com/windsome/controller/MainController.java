package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.ItemSearchDto;
import com.windsome.dto.MainItemDto;
import com.windsome.entity.*;
import com.windsome.repository.ItemImgRepository;
import com.windsome.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }
        Pageable pageable = PageRequest.of(page.orElse(0), 9);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 10);

        return "main/main";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception,
                        Model model) {
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        return "account/login";
    }
}
