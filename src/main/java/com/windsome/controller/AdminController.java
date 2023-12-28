package com.windsome.controller;

import com.windsome.dto.ItemFormDto;
import com.windsome.dto.ItemSearchDto;
import com.windsome.entity.Item;
import com.windsome.service.CategoryService;
import com.windsome.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ItemService itemService;
    private final CategoryService categoryService;

    @GetMapping("/main")
    public String home() {
        return "admin/main";
    }

    @GetMapping("/item")
    public String itemForm(Model model) throws Exception {
        model.addAttribute("itemFormDto", new ItemFormDto());
        model.addAttribute("categories", categoryService.getJsonCategories());
        model.addAttribute("formActionParam", "new");
        return "item/itemForm";
    }

    @PostMapping("/item")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                          @RequestParam(value = "parent_cate_id", required = true) Long parentCateId,
                          @RequestParam(value = "child_cate_id", required = false) Long childCateId) throws Exception {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getJsonCategories());
            return "item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            model.addAttribute("categories", categoryService.getJsonCategories());
            return "item/itemForm";
        }

        try {
            itemFormDto.setCategory(categoryService.getCategory(parentCateId, childCateId));
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            model.addAttribute("categories", categoryService.getJsonCategories());
            return "item/itemForm";
        }
        return "redirect:/";
    }

    @GetMapping("/item/{itemId}")
    public String itemUpdateForm(@PathVariable("itemId") Long itemId, Model model) throws Exception {
        model.addAttribute("categories", categoryService.getJsonCategories());
        model.addAttribute("formActionParam", itemId);
        try {
            ItemFormDto itemFormDto = itemService.getItemFormDto(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "item/itemForm";
        }
        return "item/itemForm";
    }

    @PostMapping("/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                             @RequestParam(value = "parent_cate_id", required = true) Long parentCateId,
                             @RequestParam(value = "child_cate_id", required = false) Long childCateId) throws Exception {
        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }

        try {
            itemFormDto.setCategory(categoryService.getCategory(parentCateId, childCateId));
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            model.addAttribute("categories", categoryService.getJsonCategories());
            return "item/itemForm";
        }

        return "redirect:/";
    }

    @GetMapping(value = {"/items", "/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page, Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);

        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 10);
        return "item/itemMng";
    }
}

