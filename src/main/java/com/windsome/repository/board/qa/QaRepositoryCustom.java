package com.windsome.repository.board.qa;

import com.windsome.dto.board.qa.QaListDto;
import com.windsome.dto.board.qa.QaSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QaRepositoryCustom {

    Page<QaListDto> getQaList(QaSearchDto qaSearchDto, Pageable pageable);
}
