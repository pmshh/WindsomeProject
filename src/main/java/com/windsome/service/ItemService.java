package com.windsome.service;

import com.windsome.entity.Item;
import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.ItemDto;
import com.windsome.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Item itemSave(ItemDto itemDto) {
        Item item = Item.builder()
                .itemNm(itemDto.getItemNm())
                .price(itemDto.getPrice())
                .stockNumber(itemDto.getStockNumber())
                .itemDetail(itemDto.getItemDetail())
                .itemSellStatus(ItemSellStatus.SELL)
                .discount(itemDto.getDiscount())
//                .cateCode(itemDto.getCateCode())
                .build();
        return itemRepository.save(item);
    }
}
