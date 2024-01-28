package com.windsome.service;

import com.windsome.dto.admin.DashboardDataDto;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AccountRepository accountRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

    public DashboardDataDto getDashboardData() {
        long totalAccount = accountRepository.count();
        long totalItem = itemRepository.count();
        Long totalSales = orderItemRepository.totalSales();

        return DashboardDataDto.builder()
                .totalAccount(totalAccount)
                .totalItem(totalItem)
                .totalSales(totalSales)
                .build();
    }
}
