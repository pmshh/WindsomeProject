package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.CartDetailDto;
import com.windsome.dto.CartItemDto;
import com.windsome.dto.CartOrderDto;
import com.windsome.entity.Account;
import com.windsome.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 화면
     */
    @GetMapping("/cart")
    public String cartHist(@CurrentAccount Account account, Model model) {
        model.addAttribute("cartItemTotalCount", cartService.getCartItemTotalCount(account));
        model.addAttribute("cartItems", cartService.getCartList(account.getUserIdentifier()));
        return "cart/cartList";
    }

    /**
     * 장바구니 상품 등록
     */
    @PostMapping("/cart")
    public ResponseEntity<Object> cart(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, @CurrentAccount Account account) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(sb.toString());
        }

        Long cartItemId;

        try {
            cartItemId = cartService.addCart(cartItemDto, account.getUserIdentifier());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body(cartItemId);
    }

    /**
     * 장바구니 상품 개수 변경
     */
    @PatchMapping("/cartItem/{cartItemId}")
    public ResponseEntity<Object> updateCartItem(@PathVariable("cartItemId") Long cartItemId, int count, @CurrentAccount Account account) {
        if (count <= 0) {
            return ResponseEntity.badRequest().body("최소 1개 이상 담아주세요.");
        } else if (cartService.validateCartItem(cartItemId, account.getUserIdentifier())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
        }
        cartService.updateCartItemCount(cartItemId, count);
        return ResponseEntity.ok().body(cartItemId);
    }

    /**
     * 장바구니 상품 삭제
     */
    @DeleteMapping("/cartItem/{cartItemId}")
    public ResponseEntity<Object> deleteCartItem(@PathVariable("cartItemId") Long cartItemId, @CurrentAccount Account account) {
        if (cartService.validateCartItem(cartItemId, account.getUserIdentifier())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
        }
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok().body(cartItemId);
    }
}
