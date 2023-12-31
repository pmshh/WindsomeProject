package com.windsome.service;

import com.windsome.dto.CartItemDto;
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

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final AccountRepository accountRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

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
}
