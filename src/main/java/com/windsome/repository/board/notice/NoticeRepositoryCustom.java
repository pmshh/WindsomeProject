package com.windsome.repository.board.notice;

import com.windsome.dto.board.notice.NoticeListDto;
import com.windsome.dto.board.notice.NoticeSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {

    Page<NoticeListDto> getNoticeList(NoticeSearchDto noticeSearchDto, Pageable pageable);
}
