package com.windsome.repository.product;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.constant.ProductSellStatus;
import com.windsome.dto.product.MainPageProductDTO;
import com.windsome.dto.product.ProductSearchDTO;
import com.windsome.dto.product.QMainPageProductDTO;
import com.windsome.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.windsome.entity.QProduct.*;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> findProducts(ProductSearchDTO productSearchDto, Pageable pageable) {
        QProduct product = QProduct.product;

        List<Product> content = queryFactory
                .selectFrom(product)
                .where(regTimeAfter(productSearchDto.getSearchDateType()),
                        searchSellStatusEq(productSearchDto.getSearchSellStatus()),
                        searchByLike(productSearchDto.getSearchBy(), productSearchDto.getSearchQuery()))
                .orderBy(product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(product.count())
                .from(product)
                .where(regTimeAfter(productSearchDto.getSearchDateType()),
                        searchSellStatusEq(productSearchDto.getSearchSellStatus()),
                        searchByLike(productSearchDto.getSearchBy(), productSearchDto.getSearchQuery()))
                .orderBy(product.id.desc());

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    @Override
    public Page<MainPageProductDTO> getMainPageProducts(ProductSearchDTO productSearchDto, Pageable pageable) {
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        List<MainPageProductDTO> content = queryFactory
                .select(
                        new QMainPageProductDTO(
                                product.id,
                                product.name,
                                product.category,
                                product.productDetail,
                                productImage.imageUrl,
                                product.price,
                                product.discount)
                )
                .from(productImage)
                .join(productImage.product, product)
                .where(productImage.isRepresentativeImage.eq(true))
                .where(productNameLike(productSearchDto.getSearchQuery())
                        .or(productCategoryLike(productSearchDto.getCategory()))
                        .and(product.productSellStatus.eq(ProductSellStatus.AVAILABLE)))
                .orderBy(createOrderSpecifier(productSearchDto))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(product.count())
                .from(productImage)
                .join(productImage.product, product)
                .where(productImage.isRepresentativeImage.eq(true))
                .where(productNameLike(productSearchDto.getSearchQuery()));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private OrderSpecifier[] createOrderSpecifier(ProductSearchDTO productSearchDto) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        if (Objects.isNull(productSearchDto.getSort()) || productSearchDto.getSort().equals("new") || productSearchDto.getSort().isEmpty()) {
            orderSpecifiers.add(new OrderSpecifier(Order.DESC, product.id));
        } else if (productSearchDto.getSort().equals("best")) {
            orderSpecifiers.add(new OrderSpecifier(Order.DESC, product.averageRating));
        } else if (productSearchDto.getSort().equals("low")) {
            orderSpecifiers.add(new OrderSpecifier(Order.ASC, product.price));
        } else if (productSearchDto.getSort().equals("high")) {
            orderSpecifiers.add(new OrderSpecifier(Order.DESC, product.price));
        } else if (productSearchDto.getSort().equals("name")) {
            orderSpecifiers.add(new OrderSpecifier(Order.ASC, product.name));
        }
        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }

    private BooleanBuilder productCategoryLike(Long categoryId) {
        BooleanBuilder builder = new BooleanBuilder();

        if (categoryId != null && categoryId == 100L) {
            return builder.or(product.category.id.in(100L, 101L, 102L, 103L));
        } else if (categoryId != null && categoryId == 200L) {
            return builder.or(product.category.id.in(200L, 201L, 202L, 203L, 204L));
        } else if (categoryId != null && categoryId == 300L) {
            return builder.or(product.category.id.in(300L, 301L, 302L, 303L));
        } else if (categoryId != null && categoryId == 400L) {
            return builder.or(product.category.id.in(400L));
        } else if (categoryId != null && categoryId == 500L) {
            return builder.or(product.category.id.in(500L, 501L, 502L, 503L));
        } else if (categoryId != null) {
            return builder.or(product.category.id.eq(categoryId));
        } else {
            return null;
        }
    }

    private BooleanBuilder productNameLike(String searchQuery){
        BooleanBuilder builder = new BooleanBuilder();

        builder.or(product.name.like("%" + searchQuery + "%"));
        return builder;
    }

    private BooleanExpression searchSellStatusEq(ProductSellStatus searchSellStatus) {
        return searchSellStatus == null ? null : product.productSellStatus.eq(searchSellStatus);
    }

    private BooleanExpression regTimeAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if (StringUtils.equals("all", searchDateType) || searchDateType == null) {
            return null;
        } else if (StringUtils.equals("1d", searchDateType)) {
            dateTime = dateTime.minusDays(1);
        } else if (StringUtils.equals("1w", searchDateType)) {
            dateTime = dateTime.minusWeeks(1);
        } else if (StringUtils.equals("1m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        }  else if (StringUtils.equals("6m", searchDateType)) {
            dateTime = dateTime.minusMonths(6);
        }
        return product.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if (StringUtils.equals("productName", searchBy)) {
            return product.name.like("%" + searchQuery + "%");
        } else if (StringUtils.equals("createdBy", searchBy)) {
            return product.createdBy.like("%" + searchQuery + "%");
        }
        return null;
    }
}

