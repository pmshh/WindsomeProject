package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.constant.ItemSellStatus;
import com.windsome.constant.OrderStatus;
import com.windsome.dto.*;
import com.windsome.entity.Account;
import com.windsome.entity.Item;
import com.windsome.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final ItemService itemService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final AdminService adminService;

    /**
     * 관리자 페이지 메인 화면
     */
    @GetMapping("/admin/main")
    public String home(Model model) {
        String userIdentifier = "";
        ItemSearchDto itemSearchDto = new ItemSearchDto();
        Pageable pageable = PageRequest.of(0, 3);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);
        Page<OrderMngDto> orders = orderService.getAdminPageOrderList(userIdentifier, pageable);

        DashboardDataDto dashboardData = adminService.getDashboardData();

        model.addAttribute("sellStatus", ItemSellStatus.SELL);
        model.addAttribute("orderStatus", OrderStatus.READY);
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("items", items);
        model.addAttribute("orders", orders);
        return "admin/main";
    }

    /**
     * 관리자 페이지 - 상품 관리
     */
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

    /**
     * 관리자 페이지 - 상품 등록 화면
     */
    @GetMapping("/admin/item")
    public String saveItemForm(Model model) throws Exception {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "admin/item/itemEnroll";
    }

    /**
     * 관리자 페이지 - 상품 등록
     */
    @PostMapping("/admin/item")
    public String saveItem(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
                           @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) throws Exception {
        if (bindingResult.hasErrors()) {
            return "admin/item/itemEnroll";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "admin/item/itemEnroll";
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "admin/item/itemEnroll";
        }

        redirectAttributes.addFlashAttribute("save_result", "save_ok");
        return "redirect:/admin/items";
    }

    /**
     * 관리자 페이지 - 상품 상세 화면
     */
    @GetMapping("/admin/itemDtl/{itemId}")
    public String itemDtl(PageDto pageDto, @PathVariable("itemId") Long itemId, Model model) throws Exception  {
        try {
            model.addAttribute("itemFormDto", itemService.getItemFormDto(itemId));
            model.addAttribute("pageDto", pageDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            model.addAttribute("pageDto", pageDto);
            return "admin/item/itemDtl";
        }
        return "admin/item/itemDtl";
    }

    /**
     * 관리자 페이지 - 상품 수정 화면
     */
    @GetMapping("/admin/item/{itemId}")
    public String modifyItemForm(PageDto pageDto, @PathVariable("itemId") Long itemId, Model model) throws Exception {
        try {
            model.addAttribute("itemFormDto", itemService.getItemFormDto(itemId));
            model.addAttribute("pageDto", pageDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            model.addAttribute("pageDto", pageDto);
            return "admin/item/itemUpdate";
        }
        return "admin/item/itemUpdate";
    }

    /**
     * 관리자 페이지 - 상품 수정
     */
    @PostMapping("/admin/item/{itemId}")
    public String modifyItem(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, RedirectAttributes redirectAttributes,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) throws Exception {
        if (bindingResult.hasErrors()) {
            return "admin/item/itemUpdate";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            redirectAttributes.addFlashAttribute("update_result", "required_rep_img");
            return "redirect:/admin/items";
        }

        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("update_result", "update_fail");
            return "redirect:/admin/items";
        }

        redirectAttributes.addFlashAttribute("update_result", "update_ok");
        return "redirect:/admin/items";
    }

    /**
     * 관리자 페이지 - 상품 삭제
     */
    @DeleteMapping("/admin/item/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable("itemId") Long itemId, @CurrentAccount Account account) {
        try {
            itemService.deleteItem(itemId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("일치하는 상품 정보가 없습니다.");
        }
        return ResponseEntity.ok().body(itemId);
    }

    /**
     * 관리자 페이지 - 상품 카테고리 목록(JSON) 불러오기
     */
    @GetMapping("/admin/item/categories")
    public ResponseEntity<Object> getItemCategories() throws Exception {
        String jsonCategories = categoryService.getJsonCategories();
        return ResponseEntity.ok().body(jsonCategories);
    }

    /**
     * 관리자 페이지 - 주문 관리 화면
     */
    @GetMapping(value = {"/admin/orders", "/admin/orders/{page}"})
    public String orderManage(String userIdentifier, @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        Page<OrderMngDto> orders = orderService.getAdminPageOrderList(userIdentifier == null ? "" : userIdentifier, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("maxPage", 10);
        model.addAttribute("orderStatus", OrderStatus.READY);
        return "admin/order/orderMng";
    }

    /**
     * 관리자 페이지 - 주문 취소
     */
    @PostMapping("/admin/order/{orderId}/cancel")
    public ResponseEntity<Object> cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().body(orderId);
    }
}

