package com.windsome.controller;

import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.item.ItemFormDto;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ItemControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ItemRepository itemRepository;
    @Autowired ItemImgRepository itemImgRepository;
    @Autowired ItemService itemService;

    @Test
    @DisplayName("아이템 상세 화면 보이는지 테스트")
    public void cartHist() throws Exception {
        ItemFormDto itemFormDto = getItemFormDto();
        List<MultipartFile> multipartFiles = createMultipartFiles();
        Long itemId = itemService.saveItem(itemFormDto, multipartFiles);

        mockMvc.perform(get("/item/" + itemId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("main/item/itemDtl"))
                .andExpect(model().attributeExists("item"));
    }

    private ItemFormDto getItemFormDto() {
        return new ItemFormDto(null, "test", 10000, 0.0, "test", 100, ItemSellStatus.SELL, null, null, null);
    }

    List<MultipartFile> createMultipartFiles() throws Exception {
        List<MultipartFile> multipartFileList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String path = "C:/shop/item/";
            String imageName = "imageName" + i + ".jpg";
            MockMultipartFile multipartFile = new MockMultipartFile(path, imageName, "image/jpg", new byte[]{1, 2, 3, 4});
            multipartFileList.add(multipartFile);
        }
        return multipartFileList;
    }
}