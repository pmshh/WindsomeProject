package com.windsome.service;

import com.windsome.dto.ItemFormDto;
import com.windsome.entity.Category;
import com.windsome.entity.Item;
import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.ItemDto;
import com.windsome.entity.ItemImg;
import com.windsome.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        for (int i = 0; i < itemImgFileList.size(); i++) {
            if (!itemImgFileList.get(i).isEmpty()) {
                ItemImg itemImg = new ItemImg();
                itemImg.setItem(item);

                if (i == 0)
                    itemImg.setRepImgYn("Y");
                else
                    itemImg.setRepImgYn("N");

                itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
            }
        }
        return item.getId();
    }

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
