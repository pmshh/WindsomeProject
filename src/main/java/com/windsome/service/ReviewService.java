package com.windsome.service;

import com.windsome.dto.review.ItemDtlPageReviewDto;
import com.windsome.dto.review.*;
import com.windsome.entity.*;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import com.windsome.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final AccountRepository accountRepository;
    private final ItemImgRepository itemImgRepository;

    /**
     * 리뷰 등록
     */
    public void enrollReview(ReviewEnrollDto reviewEnrollDto, Account account){
        Item item = itemRepository.findById(reviewEnrollDto.getItemId()).orElseThrow(EntityExistsException::new);
        Review review = Review.createReview(reviewEnrollDto, item, account);
        reviewRepository.save(review);
    }

    /**
     * 리뷰 등록 화면 - 상품 검색(상품 리스트 조회)
     */
    public PageImpl<ItemListDto> getItemList(ItemSearchDto searchDto, Pageable pageable) {
        List<ItemListDto> content = itemRepository.getReviewPageItemList(searchDto.getSearchQuery(), pageable);
        Long count = itemRepository.getReviewPageItemListCount(searchDto.getSearchQuery());

        return new PageImpl<ItemListDto>(content, pageable, count);
    }

    /**
     * 리뷰 등록 화면 - 상품 상세 화면에서 리뷰 작성 화면 접근 시, 리뷰 등록 화면에 해당 상품 정보 출력
     */
    public ItemDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(itemId, "Y");
        return ItemDto.createReviewEnrollPageItemDto(item, itemImg.getImgUrl());
    }

    /**
     * 리뷰 상세 화면 - 리뷰 조회
     */
    public ReviewDtlPageReviewDto getReviewDtl(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(review.getItem().getId(), "Y");
        return ReviewDtlPageReviewDto.createReviewDtlPageDto(review, itemImg);
    }

    /**
     * 리뷰 수정
     */
    public void updateReview(ReviewUpdateDto reviewUpdateDto) {
        Review findReview = reviewRepository.findById(reviewUpdateDto.getReviewId()).orElseThrow(EntityNotFoundException::new);
        findReview.updateReview(reviewUpdateDto);
        reviewRepository.save(findReview);
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        reviewRepository.delete(review);
    }

    /**
     * 리뷰 수정/삭제 권한 검증
     */
    public boolean validateReview(Long reviewId, String userIdentifier) {
        Account currentAccount = accountRepository.findByUserIdentifier(userIdentifier);
        Review review = new Review();
        try {
            review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        } catch (Exception e) {
            return true;
        }
        Account savedAccount = review.getAccount();

        return !StringUtils.equals(currentAccount.getUserIdentifier(), savedAccount.getUserIdentifier());
    }

    /**
     * 리뷰 게시판 - 리뷰 조회
     */
    public Page<ReviewListDto> getReviews(ReviewSearchDto reviewSearchDto, Pageable pageable) {
        return reviewRepository.getReviews(reviewSearchDto, pageable);
    }

    /**
     * 상품 상세 화면 - 리뷰 조회
     */
    public Page<ItemDtlPageReviewDto> getItemDtlPageReviews(Long itemId, Pageable pageable) {
        List<Review> content = reviewRepository.findByItemIdOrderByIdDesc(itemId, pageable);

        List<ItemDtlPageReviewDto> itemDtlPageReviewDtoList = new ArrayList<>();
        for (Review review : content) {
            Item item = itemRepository.findById(review.getItem().getId()).orElseThrow(EntityNotFoundException::new);

            ItemDtlPageReviewDto itemDtlPageReviewDto = ItemDtlPageReviewDto.createItemDtlPageReviewDto(review, item);
            itemDtlPageReviewDtoList.add(itemDtlPageReviewDto);
        }
        Long totalCount = reviewRepository.countByItemId(itemId);

        return new PageImpl<ItemDtlPageReviewDto>(itemDtlPageReviewDtoList, pageable, totalCount);
    }

    /**
     * 상품 리뷰 평균 평점
     */
    public void setRatingAvg(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        item.setRatingAvg(reviewRepository.getRatingAvg(itemId));
        itemRepository.save(item);
    }
}
