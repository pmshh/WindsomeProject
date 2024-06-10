package com.windsome.repository.board;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.board.notice.NoticeListDTO;
import com.windsome.dto.board.notice.QNoticeListDTO;
import com.windsome.dto.board.qa.QQaListDTO;
import com.windsome.dto.board.qa.QaListDTO;
import com.windsome.dto.board.review.QReviewListDTO;
import com.windsome.dto.board.review.ReviewListDTO;
import com.windsome.entity.board.QBoard;
import com.windsome.entity.product.QProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBoard board = QBoard.board;
    private final QProductImage productImage = QProductImage.productImage;

    @Override
    public Page<NoticeListDTO> getNoticeList(SearchDTO searchDTO, Pageable pageable) {
        List<NoticeListDTO> content = queryFactory
                .select(
                        new QNoticeListDTO(
                                board.id,
                                board.title,
                                board.content,
                                board.member.name,
                                board.regTime,
                                board.hasNotice
                        )
                )
                .from(board)
                .where(like(searchDTO))
                .where(board.member.isDeleted.eq(false))
                .where(board.boardType.eq("Notice"))
                .orderBy(board.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(board.count())
                .from(board)
                .where(like(searchDTO))
                .where(board.boardType.eq("Notice"));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    @Override
    public Page<QaListDTO> getQaList(SearchDTO searchDTO, Pageable pageable) {
        List<QaListDTO> content = queryFactory
                .select(
                        new QQaListDTO(
                                board.id,
                                board.title,
                                board.content,
                                board.member.name,
                                board.member.role,
                                board.regTime,
                                board.hasPrivate,
                                board.originNo,
                                board.groupOrder,
                                board.groupLayer
                        )
                )
                .from(board)
                .where(like(searchDTO))
                .where(board.member.isDeleted.eq(false))
                .where(board.boardType.eq("Q&A"))
                .orderBy(board.originNo.desc(), board.groupOrder.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(board.count())
                .from(board)
                .where(like(searchDTO))
                .where(board.boardType.eq("Q&A"));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    @Override
    public Page<ReviewListDTO> getReviews(SearchDTO searchDTO, Pageable pageable) {
        List<ReviewListDTO> content = queryFactory
                .select(
                        new QReviewListDTO(
                                board.id,
                                board.title,
                                board.content,
                                board.rating,
                                board.member.name,
                                board.regTime,
                                board.hits,
                                productImage.imageUrl,
                                board.product.id,
                                board.product.name,
                                board.product.price,
                                board.product.discount
                        )
                )
                .from(board)
                .join(productImage)
                .on(productImage.product.id.eq(board.product.id))
                .where(productImage.isRepresentativeImage.eq(true))
                .where(like(searchDTO))
                .where(board.member.isDeleted.eq(false))
                .where(board.boardType.eq("Review"))
                .orderBy(board.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(board.count())
                .from(board)
                .join(productImage)
                .on(productImage.product.id.eq(board.product.id))
                .where(productImage.isRepresentativeImage.eq(true))
                .where(like(searchDTO))
                .where(board.boardType.eq("Review"));


        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanExpression like(SearchDTO searchDTO) {
        if (StringUtils.equals(searchDTO.getSearchDateType(), "title")) {
            return board.title.like("%" + searchDTO.getSearchQuery() + "%");
        } else if (StringUtils.equals(searchDTO.getSearchDateType(), "content")) {
            return board.content.like("%" + searchDTO.getSearchQuery() + "%");
        } else if (StringUtils.equals(searchDTO.getSearchDateType(), "name")) {
            return board.member.name.like("%" + searchDTO.getSearchQuery() + "%");
        } else if (StringUtils.equals(searchDTO.getSearchDateType(), "")) {
            return board.title.like("%" + searchDTO.getSearchQuery() + "%")
                    .or(board.content.like("%" + searchDTO.getSearchQuery() + "%"))
                    .or(board.member.name.like("%" + searchDTO.getSearchQuery() + "%"));
        }
        return null;
    }
}
