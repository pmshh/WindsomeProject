package com.windsome.controller.admin;

import com.windsome.dto.admin.PageDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.dto.product.ProductFormDTO;
import com.windsome.exception.ProductImageDeletionException;
import com.windsome.service.admin.AdminService;
import com.windsome.service.product.CategoryService;
import com.windsome.service.product.ProductService;
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
public class AdminProductController {

    private final AdminService adminService;
    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * 상품 조회
     */
    @GetMapping("/products")
    public String showProductList(ProductSearchDTO productSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("products", adminService.getProductList(productSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("productSearchDto", productSearchDto);
        model.addAttribute("maxPage", 10);
        return "admin/product/product-management";
    }

    /**
     * 상품 등록 화면
     */
    @GetMapping("/products/new")
    public String showProductForm(Model model) {
        model.addAttribute("productFormDto", new ProductFormDTO());
        return "admin/product/product-create";
    }

    /**
     * 상품 등록
     */
    @PostMapping("/products/new")
    public String createProduct(@Valid ProductFormDTO productFormDto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
                                @RequestParam("productImageFile") List<MultipartFile> productImageFileList) throws Exception {
        if (bindingResult.hasErrors()) {
            return "admin/product/product-create";
        }

        if (productImageFileList.isEmpty() || productImageFileList.get(0).isEmpty()) { // 이미지 파일 리스트가 비어있거나 첫 번째 파일이 비어있는 경우
            model.addAttribute("message", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "admin/product/product-create";
        }

        try {
            productService.createProduct(productFormDto, productImageFileList);
            redirectAttributes.addFlashAttribute("message", "상품이 등록되었습니다.");
            return "redirect:/admin/products";
        } catch (Exception e) {
            model.addAttribute("productFormDto", productFormDto);
            model.addAttribute("message", "상품 등록 중 에러가 발생하였습니다.");
            return "admin/product/product-create";
        }
    }

    /**
     * 상품 상세 화면
     */
    @GetMapping("/products/{productId}")
    public String showProductDetail(PageDTO pageDto, @PathVariable("productId") Long productId, Model model) {
        model.addAttribute("type", "detail");
        try {
            model.addAttribute("productFormDto", productService.getProductFormDto(productId));
            model.addAttribute("pageDto", pageDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("productFormDto", new ProductFormDTO());
            model.addAttribute("pageDto", pageDto);
            return "admin/product/product-form";
        }
        return "admin/product/product-form";
    }

    /**
     * 상품 수정 화면
     */
    @GetMapping("/products/{productId}/edit")
    public String showEditProductForm(PageDTO pageDto, @PathVariable("productId") Long productId, Model model) {
        model.addAttribute("type", "update");
        try {
            model.addAttribute("productFormDto", productService.getProductFormDto(productId));
            model.addAttribute("pageDto", pageDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("productFormDto", new ProductFormDTO());
            model.addAttribute("pageDto", pageDto);
            return "admin/product/product-form";
        }
        return "admin/product/product-form";
    }

    /**
     * 상품 수정
     */
    @PostMapping("/products/{productId}")
    public String updateProduct(@Valid ProductFormDTO productFormDto, BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                @RequestParam("productImageFile") List<MultipartFile> productImageFileList) {
        if (bindingResult.hasErrors()) {
            return "admin/product/product-form";
        }

        if (productImageFileList.get(0).isEmpty() && productFormDto.getId() == null) {
            redirectAttributes.addFlashAttribute("message ", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "redirect:/admin/products";
        }

        try {
            productService.updateProduct(productFormDto, productImageFileList);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "상품 수정 도중 오류가 발생했습니다.");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("message", "상품을 수정했습니다.");
        return "redirect:/admin/products";
    }

    /**
     * 상품 이미지 삭제
     */
    @DeleteMapping("/products/{productImageId}")
    public ResponseEntity<String> deleteProductImage(@PathVariable Long productImageId) {
        try {
            productService.deleteProductImage(productImageId);
            return ResponseEntity.ok().body("이미지가 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 이미지입니다.");
        } catch (ProductImageDeletionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 상품 삭제
     */
    @DeleteMapping("/products/delete")
    public ResponseEntity<String> deleteProducts(@RequestBody Long[] productIds) {
        try {
            productService.deleteProducts(productIds);
            return ResponseEntity.ok().body("상품이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("일치하는 상품 정보가 없습니다.");
        }
    }

    /**
     * 상품 카테고리 전체 조회
     */
    @GetMapping("/products/categories")
    public ResponseEntity<String> fetchProductCategories() throws Exception {
        return ResponseEntity.ok().body(categoryService.getProductCategories());
    }
}
