package com.windsome.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.ItemSearchDto;
import com.windsome.dto.MainItemDto;
import com.windsome.dto.QMainItemDto;
import com.windsome.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.windsome.entity.QItem.item;

@RequiredArgsConstructor
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        List<Item> content = queryFactory
                .selectFrom(item)
                .where(regTimeAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(item.count())
                .from(QItem.item)
                .where(regTimeAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc());

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        List<MainItemDto> content = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                item.category,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price)
                )
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repImgYn.eq("Y"))
                .where(itemNmLike(itemSearchDto.getSearchQuery())
                        .or(itemCategoryLike(itemSearchDto.getCategory())))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(item.count())
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repImgYn.eq("Y"))
                .where(itemNmLike(itemSearchDto.getSearchQuery()));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanBuilder itemCategoryLike(Long categoryId) {
        BooleanBuilder builder = new BooleanBuilder();

        if (categoryId != null && categoryId == 100L) {
            return builder.or(item.category.id.in(100L, 101L, 102L, 103L));
        } else if (categoryId != null && categoryId == 200L) {
            return builder.or(item.category.id.in(200L, 201L, 202L, 203L, 204L));
        } else if (categoryId != null && categoryId == 300L) {
            return builder.or(item.category.id.in(300L, 301L, 302L, 303L));
        } else if (categoryId != null && categoryId == 400L) {
            return builder.or(item.category.id.in(400L));
        } else if (categoryId != null && categoryId == 500L) {
            return builder.or(item.category.id.in(500L, 501L, 502L, 503L));
        } else if (categoryId != null) {
            return builder.or(item.category.id.eq(categoryId));
        } else {
            return null;
        }
    }

    private BooleanBuilder itemNmLike(String searchQuery){
        BooleanBuilder builder = new BooleanBuilder();

        builder.or(QItem.item.itemNm.like("%" + searchQuery + "%"));
        return builder;
    }

    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }

    private BooleanExpression regTimeAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if (StringUtils.equals("all", searchDateType) || searchDateType == null) {
            return null;
        } else if (StringUtils.equals("1d", searchDateType)) {
            LocalDateTime dataTime = dateTime.minusDays(1);
        } else if (StringUtils.equals("1w", searchDateType)) {
            LocalDateTime dataTime = dateTime.minusWeeks(1);
        } else if (StringUtils.equals("1m", searchDateType)) {
            LocalDateTime dataTime = dateTime.minusMonths(1);
        }  else if (StringUtils.equals("6m", searchDateType)) {
            LocalDateTime dataTime = dateTime.minusMonths(6);
        }
        return item.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if (StringUtils.equals("itemNm", searchBy)) {
            return item.itemNm.like("%" + searchQuery + "%");
        } else if (StringUtils.equals("createdBy", searchBy)) {
            return item.createdBy.like("%" + searchQuery + "%");
        }
        return null;
    }
}
