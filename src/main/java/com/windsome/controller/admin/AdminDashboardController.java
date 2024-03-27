package com.windsome.controller.admin;

import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.service.admin.AdminService;
import com.windsome.service.board.BoardService;
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
    private final BoardService boardService;

    /**
     * dashboard
     */
    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        Pageable pageable = PageRequest.of(0, 5);
        model.addAttribute("dashboardData", adminService.getDashboardData());
        model.addAttribute("products", adminService.getProductList(new ProductSearchDTO(), pageable));
        model.addAttribute("orders", adminService.getOrderList("", pageable));
        model.addAttribute("qaList", boardService.getQaList(new SearchDTO(), pageable));
        return "admin/dashboard";
    }
}

