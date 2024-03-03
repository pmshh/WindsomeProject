package com.windsome.controller;

import com.windsome.config.security.CurrentMember;
import com.windsome.dto.cart.CartProductDto;
import com.windsome.entity.Member;
import com.windsome.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 화면
     */
    @GetMapping("/cart")
    public String getCartProducts(@CurrentMember Member member, Model model) {
        model.addAttribute("cartProducts", cartService.getCartProducts(member.getUserIdentifier()));
        return "cart/cart-list";
    }

    /**
     * 장바구니 상품 추가
     */
    @PostMapping("/cart")
    public ResponseEntity<String> addCartProduct(@Valid CartProductDto cartProductDto, BindingResult bindingResult, @CurrentMember Member member) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining()));
        }

        try {
            cartService.addCartProduct(cartProductDto, member.getUserIdentifier());
            return ResponseEntity.ok().body("장바구니에 상품이 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 장바구니 상품 개수 수정
     */
    @PatchMapping("/cart/{cartProductId}")
    public ResponseEntity<String> updateCartProductQuantity(@PathVariable("cartProductId") Long productId, int count, @CurrentMember Member member) {
        if (count <= 0) {
            return ResponseEntity.badRequest().body("최소 1개 이상 담아주세요.");
        }

        if (cartService.validateCartModificationPermission(productId, member.getUserIdentifier())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
        }

        cartService.updateCartItemQuantity(productId, count);
        return ResponseEntity.ok().body("장바구니 상품의 개수가 수정되었습니다.");
    }

    /**
     * 장바구니 상품 삭제
     */
    @DeleteMapping("/cart/delete")
    public ResponseEntity<String> deleteCartProduct(@RequestBody Long[] productIds, @CurrentMember Member member) {
        if (cartService.validateCartDeletionPermission(productIds, member.getUserIdentifier())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
        }
        cartService.deleteCartProduct(productIds);
        return ResponseEntity.ok().body("장바구니 상품이 삭제되었습니다.");
    }
}
