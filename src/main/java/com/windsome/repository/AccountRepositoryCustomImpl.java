package com.windsome.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.constant.ItemSellStatus;
import com.windsome.constant.Role;
import com.windsome.dto.account.AccountInfoDto;
import com.windsome.dto.account.AccountSearchDto;
import com.windsome.dto.account.QAccountInfoDto;
import com.windsome.entity.QAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.util.List;

import static com.windsome.entity.QAccount.*;
import static com.windsome.entity.QItem.item;

@RequiredArgsConstructor
public class AccountRepositoryCustomImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AccountInfoDto> getAccountInfo(AccountSearchDto accountSearchDto, Pageable pageable) {
        QAccount account = QAccount.account;

        List<AccountInfoDto> content = queryFactory
                .select(
                        new QAccountInfoDto(
                                account.id,
                                account.userIdentifier,
                                account.email,
                                account.name,
                                account.address1,
                                account.address2,
                                account.address3,
                                account.state,
                                account.point,
                                account.totalPoint,
                                account.totalUsePoint,
                                account.totalOrderPrice,
                                account.regTime
                        )
                )
                .from(account)
                .where(like(accountSearchDto.getSearchType(), accountSearchDto.getSearchQuery()),
                        (searchStateTypeEq(accountSearchDto.getSearchStateType())))
                .orderBy(account.state.asc(), account.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(account.count())
                .from(account)
                .where(like(accountSearchDto.getSearchType(), accountSearchDto.getSearchQuery()),
                        (searchStateTypeEq(accountSearchDto.getSearchStateType())));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanExpression like(String searchType, String searchQuery) {
        if (StringUtils.equals(searchType, "name")) {
            return account.name.like("%" + searchQuery + "%");
        } else if (StringUtils.equals(searchType, "userIdentifier")) {
            return account.userIdentifier.like("%" + searchQuery + "%");
        } else if (StringUtils.equals(searchType, "email")) {
            return account.email.like("%" + searchQuery + "%");
        }
        return null;
    }

    private BooleanExpression searchStateTypeEq(Role searchStateType) {
        return searchStateType == null ? null : QAccount.account.state.eq(searchStateType);
    }
}
