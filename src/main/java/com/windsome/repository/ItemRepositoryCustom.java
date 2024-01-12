package com.windsome.repository;

import com.windsome.dto.ItemSearchDto;
import com.windsome.entity.Item;
import com.windsome.dto.MainItemDto;
import com.windsome.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemRepositoryCustom {

    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
}
