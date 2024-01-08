package com.windsome.controller;

import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.ItemFormDto;
import com.windsome.dto.ItemSearchDto;
import com.windsome.entity.Category;
import com.windsome.entity.Item;
import com.windsome.service.CategoryService;
import com.windsome.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;

    @GetMapping("/item/{itemId}")
    public String itemDetail(Model model, @PathVariable("itemId") Long itemId) {
        ItemFormDto itemFormDto = itemService.getItemFormDto(itemId);
        model.addAttribute("item", itemFormDto);
        return "main/item/itemDtl";
    }

    @GetMapping("/admin/item")
    public String saveItemForm(Model model) throws Exception {
        model.addAttribute("itemFormDto", new ItemFormDto());
        model.addAttribute("formActionParam", "");
        return "admin/item/itemForm";
    }

    @PostMapping("/admin/item")
    public String saveItem(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                          @RequestParam(value = "parent_cate_id", required = true) Long parentCateId,
                          @RequestParam(value = "child_cate_id", required = false) Long childCateId) throws Exception {
        if (bindingResult.hasErrors()) {
            return "admin/item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "admin/item/itemForm";
        }

        try {
            Category category = categoryService.getCategory(parentCateId, childCateId);
            itemFormDto.setCategory(category);
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "admin/item/itemForm";
        }
        return "redirect:/admin/items";
    }

    @GetMapping("/admin/item/{itemId}")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) throws Exception {
        model.addAttribute("formActionParam", itemId);
        try {
            ItemFormDto itemFormDto = itemService.getItemFormDto(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "admin/item/itemForm";
        }
        return "admin/item/itemForm";
    }

    @PostMapping("/admin/item/{itemId}")
    public String updateItem(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                             @RequestParam(value = "parent_cate_id", required = true) Long parentCateId,
                             @RequestParam(value = "child_cate_id", required = false) Long childCateId) throws Exception {
        if (bindingResult.hasErrors()) {
            return "admin/item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "admin/item/itemForm";
        }

        try {
            Category category = categoryService.getCategory(parentCateId, childCateId);
            itemFormDto.setCategory(category);
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "admin/item/itemForm";
        }

        return "redirect:/";
    }

    @GetMapping("/admin/item/categories")
    public ResponseEntity<Object> getItemCategories() throws Exception {
        String jsonCategories = categoryService.getJsonCategories();
        return ResponseEntity.ok().body(jsonCategories);
    }
}
