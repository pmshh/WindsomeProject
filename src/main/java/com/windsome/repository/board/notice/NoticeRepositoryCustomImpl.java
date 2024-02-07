package com.windsome.repository.board.notice;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.dto.board.notice.NoticeListDto;
import com.windsome.dto.board.notice.NoticeSearchDto;
import com.windsome.dto.board.notice.QNoticeListDto;
import com.windsome.entity.board.QNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.util.List;

import static com.windsome.entity.board.QNotice.*;

@RequiredArgsConstructor
public class NoticeRepositoryCustomImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<NoticeListDto> getNoticeList(NoticeSearchDto noticeSearchDto, Pageable pageable) {
        QNotice notice = QNotice.notice;

        List<NoticeListDto> content = queryFactory
                .select(
                        new QNoticeListDto(
                                notice.id,
                                notice.title,
                                notice.content,
                                notice.account.name,
                                notice.regTime,
                                notice.noticeYN
                        )
                )
                .from(notice)
                .where(like(noticeSearchDto))
                .orderBy(notice.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(notice.count())
                .from(notice)
                .where(like(noticeSearchDto));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanExpression like(NoticeSearchDto noticeSearchDto) {
        if (StringUtils.equals(noticeSearchDto.getSearchDateType(), "title")) {
            return notice.title.like("%" + noticeSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(noticeSearchDto.getSearchDateType(), "content")) {
            return notice.content.like("%" + noticeSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(noticeSearchDto.getSearchDateType(), "name")) {
            return notice.account.name.like("%" + noticeSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(noticeSearchDto.getSearchDateType(), "")) {
            return notice.title.like("%" + noticeSearchDto.getSearchQuery() + "%")
                    .or(notice.content.like("%" + noticeSearchDto.getSearchQuery() + "%"))
                    .or(notice.account.name.like("%" + noticeSearchDto.getSearchQuery() + "%"));
        }
        return null;
    }
}
