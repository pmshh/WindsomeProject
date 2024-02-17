package com.windsome.controller.admin;

import com.windsome.dto.board.qa.QaSearchDto;
import com.windsome.dto.item.ItemSearchDto;
import com.windsome.service.*;
import com.windsome.service.board.QaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminService adminService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final QaService qaService;

    /**
     * dashboard
     */
    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        Pageable pageable = PageRequest.of(0, 3);
        model.addAttribute("dashboardData", adminService.getDashboardData());
        model.addAttribute("items", itemService.getAdminItemPage(new ItemSearchDto(), pageable));
        model.addAttribute("orders", orderService.getAdminPageOrderList("", pageable));
        model.addAttribute("qaList", qaService.getQaList(new QaSearchDto(), pageable));
        return "admin/dashboard";
    }
}

