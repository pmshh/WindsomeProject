package com.windsome.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.dto.review.QReviewListDto;
import com.windsome.dto.review.ReviewListDto;
import com.windsome.dto.review.ReviewSearchDto;
import com.windsome.entity.QItemImg;
import com.windsome.entity.QReview;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.util.List;

import static com.windsome.entity.QReview.*;

@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReviewListDto> getReviews(ReviewSearchDto reviewSearchDto, Pageable pageable) {
        QReview review = QReview.review;
        QItemImg itemImg = QItemImg.itemImg;

        List<ReviewListDto> content = queryFactory
                .select(
                        new QReviewListDto(
                                review.id,
                                review.title,
                                review.rating,
                                review.account.name,
                                review.regDate,
                                review.hits,
                                itemImg.imgUrl,
                                review.item.id,
                                review.item.itemNm,
                                review.item.price,
                                review.item.discount
                        )
                )
                .from(review)
                .join(itemImg)
                .on(itemImg.item.id.eq(review.item.id))
                .where(itemImg.repImgYn.eq("Y"))
                .where(like(reviewSearchDto))
                .orderBy(review.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(review.count())
                .from(review)
                .join(itemImg)
                .on(itemImg.item.id.eq(review.item.id))
                .where(itemImg.repImgYn.eq("Y"))
                .where(like(reviewSearchDto));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanExpression like(ReviewSearchDto reviewSearchDto) {
        if (StringUtils.equals(reviewSearchDto.getSearchDateType(), "title")) {
            return review.title.like("%" + reviewSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(reviewSearchDto.getSearchDateType(), "content")) {
            return review.content.like("%" + reviewSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(reviewSearchDto.getSearchDateType(), "name")) {
            return review.account.name.like("%" + reviewSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(reviewSearchDto.getSearchDateType(), "")) {
            return review.title.like("%" + reviewSearchDto.getSearchQuery() + "%")
                    .or(review.content.like("%" + reviewSearchDto.getSearchQuery() + "%"))
                    .or(review.account.name.like("%" + reviewSearchDto.getSearchQuery() + "%"));
        }
        return null;
    }
}
