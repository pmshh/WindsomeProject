package com.windsome.repository;

import com.windsome.dto.review.ReviewListDto;
import com.windsome.dto.review.ReviewSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {

    Page<ReviewListDto> getReviews(ReviewSearchDto reviewSearchDto, Pageable pageable);
}
