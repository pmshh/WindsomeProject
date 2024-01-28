package com.windsome.service;

import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.item.ItemFormDto;
import com.windsome.entity.Item;
import com.windsome.entity.ItemImg;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ItemServiceTest {

    @Autowired ItemService itemService;
    @Autowired ItemRepository itemRepository;
    @Autowired ItemImgRepository itemImgRepository;

    @Test
    @DisplayName("상품 등록 테스트")
    void saveItem() throws Exception {
        // given
        ItemFormDto itemFormDto = getItemFormDto("제목", "상세 내용");
        List<MultipartFile> multipartFileList = createMultipartFiles("이미지 제목");

        // when
        Long itemId = itemService.saveItem(itemFormDto, multipartFileList);
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);

        // then
        assertEquals(itemFormDto.getItemNm(), item.getItemNm());
        assertEquals(itemFormDto.getItemDetail(), item.getItemDetail());
        assertEquals(multipartFileList.get(0).getOriginalFilename(), itemImgList.get(0).getOriImgName());
    }

    @Test
    @DisplayName("상품 수정 테스트")
    void updateItem() throws Exception {
        // given
        ItemFormDto itemFormDto = getItemFormDto("제목", "상세 내용");
        List<MultipartFile> multipartFileList = createMultipartFiles("이미지 제목");

        Long itemId = itemService.saveItem(itemFormDto, multipartFileList);
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);

        List<Long> itemImgIds = new ArrayList<>();
        for (ItemImg itemImg : itemImgList) {
            itemImgIds.add(itemImg.getId());
        }

        ItemFormDto newItemFormDto = getItemFormDto("제목 수정", "상세 내용 수정");
        newItemFormDto.setId(itemId);
        newItemFormDto.setItemImgIds(itemImgIds);
        List<MultipartFile> newMultipartFileList = createMultipartFiles("제목 수정");

        // when
        itemService.updateItem(newItemFormDto, newMultipartFileList);
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);

        // then
        assertEquals(item.getItemNm(), newItemFormDto.getItemNm());
        assertEquals(item.getItemDetail(), newItemFormDto.getItemDetail());
        assertEquals(itemImgList.get(0).getOriImgName(), newMultipartFileList.get(0).getOriginalFilename());
    }

    private ItemFormDto getItemFormDto(String itemNm, String itemDetail) {
        return new ItemFormDto(null, "test", 10000, 0.0, "test", 100, ItemSellStatus.SELL, null, null, null);
    }

    List<MultipartFile> createMultipartFiles(String imageNameParam) throws Exception {
        List<MultipartFile> multipartFileList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String path = "C:/shop/item/";
            String imageName = imageNameParam + i + ".jpg";
            MockMultipartFile multipartFile = new MockMultipartFile(path, imageName, "image/jpg", new byte[]{1, 2, 3, 4});
            multipartFileList.add(multipartFile);
        }
        return multipartFileList;
    }
}