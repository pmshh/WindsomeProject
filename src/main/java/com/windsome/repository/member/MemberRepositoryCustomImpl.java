package com.windsome.repository.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.constant.Role;
import com.windsome.dto.member.MemberListResponseDTO;
import com.windsome.dto.member.MemberListSearchDto;
import com.windsome.dto.member.QMemberListResponseDTO;
import com.windsome.entity.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.util.List;

import static com.windsome.entity.QMember.*;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MemberListResponseDTO> findMembersByCriteria(MemberListSearchDto memberListSearchDto, Pageable pageable) {
        QMember member = QMember.member;

        List<MemberListResponseDTO> content = queryFactory
                .select(
                        new QMemberListResponseDTO(
                                member.id,
                                member.userIdentifier,
                                member.email,
                                member.name,
                                member.address1,
                                member.address2,
                                member.address3,
                                member.state,
                                member.point,
                                member.totalPoint,
                                member.totalUsePoint,
                                member.totalOrderPrice,
                                member.regTime
                        )
                )
                .from(member)
                .where(like(memberListSearchDto.getSearchType(), memberListSearchDto.getSearchQuery()),
                        (searchStateTypeEq(memberListSearchDto.getSearchStateType())))
                .orderBy(member.state.asc(), member.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(member.count())
                .from(member)
                .where(like(memberListSearchDto.getSearchType(), memberListSearchDto.getSearchQuery()),
                        (searchStateTypeEq(memberListSearchDto.getSearchStateType())));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanExpression like(String searchType, String searchQuery) {
        if (StringUtils.equals(searchType, "name")) {
            return member.name.like("%" + searchQuery + "%");
        } else if (StringUtils.equals(searchType, "userIdentifier")) {
            return member.userIdentifier.like("%" + searchQuery + "%");
        } else if (StringUtils.equals(searchType, "email")) {
            return member.email.like("%" + searchQuery + "%");
        }
        return null;
    }

    private BooleanExpression searchStateTypeEq(Role searchStateType) {
        return searchStateType == null ? null : member.state.eq(searchStateType);
    }
}
