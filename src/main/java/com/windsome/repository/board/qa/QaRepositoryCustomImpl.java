package com.windsome.repository.board.qa;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.dto.board.qa.QQaListDto;
import com.windsome.dto.board.qa.QaListDto;
import com.windsome.dto.board.qa.QaSearchDto;
import com.windsome.entity.board.QQa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.util.List;

import static com.windsome.entity.board.QQa.*;

@RequiredArgsConstructor
public class QaRepositoryCustomImpl implements QaRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<QaListDto> getQaList(QaSearchDto qaSearchDto, Pageable pageable) {
        QQa qa = QQa.qa;

        List<QaListDto> content = queryFactory
                .select(
                        new QQaListDto(
                                qa.id,
                                qa.title,
                                qa.content,
                                qa.member.name,
                                qa.member.role,
                                qa.regTime,
                                qa.secretYN,
                                qa.originNo,
                                qa.groupOrd,
                                qa.groupLayer
                        )
                )
                .from(qa)
                .where(like(qaSearchDto))
                .orderBy(qa.originNo.desc(), qa.groupOrd.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(qa.count())
                .from(qa)
                .where(like(qaSearchDto));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanExpression like(QaSearchDto qaSearchDto) {
        if (StringUtils.equals(qaSearchDto.getSearchDateType(), "title")) {
            return qa.title.like("%" + qaSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(qaSearchDto.getSearchDateType(), "content")) {
            return qa.content.like("%" + qaSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(qaSearchDto.getSearchDateType(), "name")) {
            return qa.member.name.like("%" + qaSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(qaSearchDto.getSearchDateType(), "")) {
            return qa.title.like("%" + qaSearchDto.getSearchQuery() + "%")
                    .or(qa.content.like("%" + qaSearchDto.getSearchQuery() + "%"))
                    .or(qa.member.name.like("%" + qaSearchDto.getSearchQuery() + "%"));
        }
        return null;
    }
}
