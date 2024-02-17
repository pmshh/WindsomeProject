package com.windsome.controller.admin;

import com.windsome.dto.admin.PageDto;
import com.windsome.dto.item.ItemFormDto;
import com.windsome.dto.item.ItemSearchDto;
import com.windsome.exception.ProductImageDeletionException;
import com.windsome.service.CategoryService;
import com.windsome.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;

    /**
     * 상품 관리 - 상품 조회
     */
    @GetMapping("/items")
    public String getItemList(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("items", itemService.getAdminItemPage(itemSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 10);
        return "admin/item/itemMng";
    }

    /**
     * 상품 관리 - 상품 등록 화면
     */
    @GetMapping("/items/new")
    public String showItemForm(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "admin/item/itemEnroll";
    }

    /**
     * 상품 관리 - 상품 등록
     */
    @PostMapping("/items/new")
    public String enrollItem(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) throws Exception {
        if (bindingResult.hasErrors()) {
            return "admin/item/itemEnroll";
        }

        if (itemImgFileList.isEmpty() || itemImgFileList.get(0).isEmpty()) { // 이미지 파일 리스트가 비어있거나 첫 번째 파일이 비어있는 경우
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "admin/item/itemEnroll";
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
            redirectAttributes.addFlashAttribute("save_result", "save_ok");
            return "redirect:/admin/items";
        } catch (Exception e) {
            model.addAttribute("itemFormDto", itemFormDto);
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "admin/item/itemEnroll";
        }
    }

    /**
     * 상품 관리 - 상품 상세 화면
     */
    @GetMapping("/items/{itemId}")
    public String viewItemDetail(PageDto pageDto, @PathVariable("itemId") Long itemId, Model model) {
        model.addAttribute("type", "detail");
        try {
            model.addAttribute("itemFormDto", itemService.getItemFormDto(itemId));
            model.addAttribute("pageDto", pageDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            model.addAttribute("pageDto", pageDto);
            return "admin/item/itemForm";
        }
        return "admin/item/itemForm";
    }

    /**
     * 상품 관리 - 상품 수정 화면
     */
    @GetMapping("/items/{itemId}/edit")
    public String showUpdateItemForm(PageDto pageDto, @PathVariable("itemId") Long itemId, Model model) {
        model.addAttribute("type", "update");
        try {
            model.addAttribute("itemFormDto", itemService.getItemFormDto(itemId));
            model.addAttribute("pageDto", pageDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            model.addAttribute("pageDto", pageDto);
            return "admin/item/itemForm";
        }
        return "admin/item/itemForm";
    }

    /**
     * 상품 관리 - 상품 수정
     */
    @PostMapping("/items/{itemId}")
    public String updateItem(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, RedirectAttributes redirectAttributes,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) {
        if (bindingResult.hasErrors()) {
            return "admin/item/itemForm";
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
     * 상품 관리 - 상품 이미지 삭제
     */
    @PatchMapping("/items/{itemImgId}")
    public ResponseEntity<String> deleteItemImage(@PathVariable Long itemImgId) {
        try {
            itemService.deleteItemImg(itemImgId);
            return ResponseEntity.ok().body("이미지가 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 이미지입니다.");
        } catch (ProductImageDeletionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 상품 관리 - 상품 삭제
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable("itemId") Long itemId) {
        try {
            itemService.deleteItem(itemId);
            return ResponseEntity.ok().body("상품이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("일치하는 상품 정보가 없습니다.");
        }
    }

    /**
     * 상품 관리 - 카테고리 조회
     */
    @GetMapping("/items/categories")
    public ResponseEntity<String> getItemCategories() throws Exception {
        return ResponseEntity.ok().body(categoryService.getJsonCategories());
    }
}
