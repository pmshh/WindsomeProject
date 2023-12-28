package com.windsome.repository;

import com.windsome.dto.ItemSearchDto;
import com.windsome.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {

    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
}
