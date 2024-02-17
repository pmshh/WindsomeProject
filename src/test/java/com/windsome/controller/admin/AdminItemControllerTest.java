package com.windsome.controller.admin;

import com.windsome.dto.admin.PageDto;
import com.windsome.dto.item.ItemFormDto;
import com.windsome.dto.item.ItemSearchDto;
import com.windsome.exception.ProductImageDeletionException;
import com.windsome.service.CategoryService;
import com.windsome.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminItemController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@MockBean(JpaMetamodelMappingContext.class)
class AdminItemControllerTest {

    @MockBean ItemService itemService;
    @MockBean CategoryService categoryService;
    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("상품 조회 기능 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getItemList() throws Exception {
        given(itemService.getAdminItemPage(any(ItemSearchDto.class), any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/admin/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/item/itemMng"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("itemSearchDto"))
                .andExpect(model().attributeExists("maxPage"));
    }

    @Test
    @DisplayName("상품 등록 화면 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void showItemFormTest() throws Exception {
        mockMvc.perform(get("/admin/items/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/item/itemEnroll"))
                .andExpect(model().attributeExists("itemFormDto"));
    }

    @Test
    @DisplayName("상품 등록 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void enrollItemTest() throws Exception {
        // Mocking
        given(itemService.saveItem(any(ItemFormDto.class), anyList())).willReturn(1L);

        // Creating MockMultipartFile
        MockMultipartFile mockMultipartFile = new MockMultipartFile("itemImgFile", "test.jpg", "image/jpeg", "test image content".getBytes());

        // Performing request
        mockMvc.perform(multipart("/admin/items/new")
                        .file(mockMultipartFile)
                        .param("itemNm", "Test 상품명")
                        .param("price", "10000")
                        .param("discount", "0")
                        .param("itemDetail", "Test 상품 상세")
                        .param("stockNumber", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/items"))
                .andExpect(flash().attributeExists("save_result"))
                .andExpect(flash().attribute("save_result", "save_ok"));
    }

    @Test
    @DisplayName("상품 상세 화면 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void viewItemDetailTest() throws Exception {
        // Mocking
        Long itemId = 1L;
        ItemFormDto itemFormDto = new ItemFormDto();
        PageDto pageDto = new PageDto();
        given(itemService.getItemFormDto(itemId)).willReturn(itemFormDto);

        // Performing request
        mockMvc.perform(get("/admin/items/{itemId}", itemId)
                        .param("page", "0")
                        .param("searchDateType", "")
                        .param("searchBy", "")
                        .param("searchQuery", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/item/itemForm"))
                .andExpect(model().attributeExists("type"))
                .andExpect(model().attribute("type", "detail"))
                .andExpect(model().attributeExists("itemFormDto"))
                .andExpect(model().attributeExists("pageDto"));
    }

    @Test
    @DisplayName("상품 수정 화면 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void showUpdateItemFormTest() throws Exception {
        // Mocking
        Long itemId = 1L;
        ItemFormDto itemFormDto = new ItemFormDto();
        PageDto pageDto = new PageDto();
        given(itemService.getItemFormDto(itemId)).willReturn(itemFormDto);

        // Performing request
        mockMvc.perform(get("/admin/items/{itemId}/edit", itemId)
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/item/itemForm"))
                .andExpect(model().attributeExists("type"))
                .andExpect(model().attribute("type", "update"))
                .andExpect(model().attributeExists("itemFormDto"))
                .andExpect(model().attributeExists("pageDto"));
    }

    @Test
    @DisplayName("상품 수정 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateItemTest() throws Exception {
        // Mocking
        given(itemService.updateItem(any(ItemFormDto.class), anyList())).willReturn(1L);

        // Creating MockMultipartFile
        MockMultipartFile mockMultipartFile = new MockMultipartFile("itemImgFile", "test.jpg", "image/jpeg", "test image content".getBytes());

        // Performing request
        mockMvc.perform(multipart("/admin/items/{itemId}", 1L)
                        .file(mockMultipartFile)
                        .param("itemNm", "test")
                        .param("price", "10000")
                        .param("itemDetail", "Test 상세 설명")
                        .param("stockNumber", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/items"))
                .andExpect(flash().attributeExists("update_result"))
                .andExpect(flash().attribute("update_result", "update_ok"));
    }

    @Test
    @DisplayName("상품 이미지 삭제 성공 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteItemImageSuccessTest() throws Exception {
        // Mocking - 이미지 삭제가 성공하는 경우
        doNothing().when(itemService).deleteItemImg(anyLong());

        // Perform & Verify
        mockMvc.perform(patch("/admin/items/{itemImgId}", 123L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("이미지가 삭제되었습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("상품 이미지 삭제 실패 - 존재하지 않는 이미지")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteItemImageFailureNotFoundTest() throws Exception {
        // Mocking - 존재하지 않는 이미지 삭제 시 EntityNotFoundException이 발생하는 경우
        doThrow(EntityNotFoundException.class).when(itemService).deleteItemImg(anyLong());

        // Perform & Verify
        mockMvc.perform(patch("/admin/items/{itemImgId}", 123L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("존재하지 않는 이미지입니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("상품 이미지 삭제 실패 - 이미지 삭제 중 에러 발생")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteItemImageFailureErrorTest() throws Exception {
        // Mocking - 이미지 삭제 중 에러가 발생하는 경우
        String errorMessage = "이미지 삭제 중 에러가 발생했습니다.";
        doThrow(new ProductImageDeletionException(errorMessage)).when(itemService).deleteItemImg(anyLong());

        // Perform & Verify
        mockMvc.perform(patch("/admin/items/{itemImgId}", 123L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes(errorMessage.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("상품 삭제 테스트 - 성공")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteItemSuccessTest() throws Exception {
        // Mocking - 상품 삭제가 성공하는 경우
        doNothing().when(itemService).deleteItem(anyLong());

        // Perform & Verify
        mockMvc.perform(delete("/admin/items/{itemId}", 123L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("상품이 삭제되었습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("상품 삭제 테스트 - 실패")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteItemFailureTest() throws Exception {
        // Mocking - 일치하는 상품 정보가 없는 경우
        doThrow(new Exception()).when(itemService).deleteItem(anyLong());

        // Perform & Verify
        mockMvc.perform(delete("/admin/items/{itemId}", 123L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("일치하는 상품 정보가 없습니다.".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("카테고리 조회 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getItemCategoriesTest() throws Exception {
        // Perform & Verify
        mockMvc.perform(get("/admin/items/categories").with(csrf()))
                .andExpect(status().isOk());
    }

}