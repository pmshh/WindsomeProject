package com.windsome.repository.board.review;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.windsome.dto.board.review.QReviewListDto;
import com.windsome.dto.board.review.ReviewListDto;
import com.windsome.dto.board.review.ReviewSearchDto;
import com.windsome.entity.board.QReview;
import com.windsome.entity.product.QProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.thymeleaf.util.StringUtils;

import java.util.List;

import static com.windsome.entity.board.QReview.*;

@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReviewListDto> getReviews(ReviewSearchDto reviewSearchDto, Pageable pageable) {
        QReview review = QReview.review;
        QProductImage productImage = QProductImage.productImage;

        List<ReviewListDto> content = queryFactory
                .select(
                        new QReviewListDto(
                                review.id,
                                review.title,
                                review.content,
                                review.rating,
                                review.member.name,
                                review.regDate,
                                review.hits,
                                productImage.imageUrl,
                                review.product.id,
                                review.product.name,
                                review.product.price,
                                review.product.discount
                        )
                )
                .from(review)
                .join(productImage)
                .on(productImage.product.id.eq(review.product.id))
                .where(productImage.isRepresentativeImage.eq(true))
                .where(like(reviewSearchDto))
                .orderBy(review.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(review.count())
                .from(review)
                .join(productImage)
                .on(productImage.product.id.eq(review.product.id))
                .where(productImage.isRepresentativeImage.eq(true))
                .where(like(reviewSearchDto));

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanExpression like(ReviewSearchDto reviewSearchDto) {
        if (StringUtils.equals(reviewSearchDto.getSearchDateType(), "title")) {
            return review.title.like("%" + reviewSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(reviewSearchDto.getSearchDateType(), "content")) {
            return review.content.like("%" + reviewSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(reviewSearchDto.getSearchDateType(), "name")) {
            return review.member.name.like("%" + reviewSearchDto.getSearchQuery() + "%");
        } else if (StringUtils.equals(reviewSearchDto.getSearchDateType(), "")) {
            return review.title.like("%" + reviewSearchDto.getSearchQuery() + "%")
                    .or(review.content.like("%" + reviewSearchDto.getSearchQuery() + "%"))
                    .or(review.member.name.like("%" + reviewSearchDto.getSearchQuery() + "%"));
        }
        return null;
    }
}
