package com.windsome.service;

import com.windsome.dto.CartDetailDto;
import com.windsome.dto.CartItemDto;
import com.windsome.dto.CartOrderDto;
import com.windsome.dto.OrderDto;
import com.windsome.entity.Account;
import com.windsome.entity.Cart;
import com.windsome.entity.CartItem;
import com.windsome.entity.Item;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.CartItemRepository;
import com.windsome.repository.CartRepository;
import com.windsome.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final AccountRepository accountRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String userIdentifier) {
        Item item = itemRepository.findById(cartItemDto.getItemId()).orElseThrow(EntityNotFoundException::new);
        Account account = accountRepository.findByUserIdentifier(userIdentifier);

        Cart cart = cartRepository.findByAccountId(account.getId());
        if (cart == null) {
            cart = Cart.createCart(account);
            cartRepository.save(cart);
        }

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if (savedCartItem != null) {
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
    }

    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        cartItem.updateCount(count);
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String userIdentifier) {
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Account account = accountRepository.findByUserIdentifier(userIdentifier);
        Cart cart = cartRepository.findByAccountId(account.getId());
        if (cart == null) {
            return cartDetailDtoList;
        }

        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());

        return cartDetailDtoList;
    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String userIdentifier) {
        Account currentAccount = accountRepository.findByUserIdentifier(userIdentifier);
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        Account savedAccount = cartItem.getCart().getAccount();

        if (!StringUtils.equals(currentAccount.getUserIdentifier(), savedAccount.getUserIdentifier())) {
            return false;
        }

        return true;
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String userIdentifier) {
        List<OrderDto> orderDtoList = new ArrayList<>();
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId()).orElseThrow(EntityNotFoundException::new);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        Long orderId = orderService.orders(orderDtoList, userIdentifier);

        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId()).orElseThrow(EntityNotFoundException::new);
            cartItemRepository.delete(cartItem);
        }
        return orderId;
    }
}
