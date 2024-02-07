package com.windsome.repository.board.review;

import com.windsome.dto.board.review.ReviewListDto;
import com.windsome.dto.board.review.ReviewSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {

    Page<ReviewListDto> getReviews(ReviewSearchDto reviewSearchDto, Pageable pageable);
}
