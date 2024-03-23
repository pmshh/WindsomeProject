package com.windsome.repository.board;

import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.board.notice.NoticeListDTO;
import com.windsome.dto.board.qa.QaListDTO;
import com.windsome.dto.board.review.ReviewListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardRepositoryCustom {

    Page<NoticeListDTO> getNoticeList(SearchDTO searchDTO, Pageable pageable);

    Page<QaListDTO> getQaList(SearchDTO searchDTO, Pageable pageable);

    Page<ReviewListDTO> getReviews(SearchDTO searchDTO, Pageable pageable);
}
