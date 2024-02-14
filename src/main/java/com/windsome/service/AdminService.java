package com.windsome.service;

import com.windsome.dto.admin.DashboardDataDto;
import com.windsome.dto.admin.NumOfSalesByCateDto;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.OrderItemRepository;
import com.windsome.repository.board.qa.QaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AccountRepository accountRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final QaRepository qaRepository;

    public DashboardDataDto getDashboardData() {
        long totalAccount = accountRepository.count();
        long totalItem = itemRepository.count();
        long totalQa = qaRepository.count();
        Long totalSales = orderItemRepository.totalSales();
        List<NumOfSalesByCateDto> numOfSalesByCateDtoList = orderItemRepository.numberOfSalesByCategory().stream()
                .map(NumOfSalesByCateDto::new)
                .collect(Collectors.toList());

        return DashboardDataDto.builder()
                .totalAccount(totalAccount)
                .totalItem(totalItem)
                .totalQa(totalQa)
                .totalSales(totalSales)
                .numOfSalesByCateList(numOfSalesByCateDtoList)
                .build();
    }
}
