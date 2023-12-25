package com.windsome.controller;

import com.windsome.dto.ItemFormDto;
import com.windsome.service.CategoryService;
import com.windsome.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

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

    @GetMapping("/item/new")
    public String itemForm(Model model) throws Exception {
        model.addAttribute("itemFormDto", new ItemFormDto());
        model.addAttribute("categories", categoryService.getJsonCategories());
        return "item/itemForm";
    }

    @PostMapping("/item/new")
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
}
