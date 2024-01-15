package com.windsome.service;

import com.windsome.dto.*;
import com.windsome.entity.*;
import com.windsome.constant.ItemSellStatus;
import com.windsome.repository.CategoryRepository;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImgRepository itemImgRepository;
    private final ItemImgService itemImgService;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    @Value("${itemImgLocation}")
    private String itemImgLocation;


    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        Category category = categoryRepository.findById(itemFormDto.getCategoryId()).orElseThrow();
        Item item = itemFormDto.toEntity();
        item.setCategory(category);
        itemRepository.save(item);

        for (int i = 0; i < itemImgFileList.size(); i++) {
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);

            if (i == 0)
                itemImg.setRepImgYn("Y");
            else
                itemImg.setRepImgYn("N");

            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }
        return item.getId();
    }

    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        Category category = categoryRepository.findById(itemFormDto.getCategoryId()).orElseThrow(EntityNotFoundException::new);
        Item item = itemRepository.findById(itemFormDto.getId()).orElseThrow(EntityNotFoundException::new);
        item.setCategory(category);
        item.updateItem(itemFormDto);

        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        for (int i = 0; i < itemImgFileList.size(); i++) {
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }
        return item.getId();
    }

    @Transactional(readOnly = true)
    public ItemFormDto getItemFormDto(Long itemId) {
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);

        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        for (ItemImg itemImg : itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.toDto(itemImg);
            itemImgDtoList.add(itemImgDto);
        }

        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = ItemFormDto.toDto(item);
        itemFormDto.setCategoryId(item.getCategory().getId());
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

    public void deleteItem(Long itemId) throws Exception {
        List<ItemImg> itemImgList = itemImgRepository.findByItemId(itemId).orElseThrow(EntityNotFoundException::new);
        itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);

        itemImgRepository.deleteByItemId(itemId);
        itemRepository.deleteById(itemId);

        for (ItemImg itemImg : itemImgList) {
            // 기존 이미지 삭제
            if (!StringUtils.isEmpty(itemImg.getImgName())) {
                fileService.deleteFile(itemImgLocation + "/" + itemImg.getImgName());
            }
        }
    }
}
