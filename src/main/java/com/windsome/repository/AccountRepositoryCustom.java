package com.windsome.repository;

import com.windsome.dto.account.AccountInfoDto;
import com.windsome.dto.account.AccountSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountRepositoryCustom {

    Page<AccountInfoDto> getAccountInfo (AccountSearchDto accountSearchDto, Pageable pageable);
}
