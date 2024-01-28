package com.windsome.service;

import com.windsome.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ReviewServiceTest {

    @Autowired
    ReviewService reviewService;
    @Autowired
    ReviewRepository reviewRepository;

//    @Test
//    @DisplayName("댓글 등록 테스트")
//    public void enrollReplyTest() {
//        ReviewDto reviewDto = new ReviewDto();
//        Long replyId = reviewService.enrollReview(reviewDto);
//        Review review = reviewRepository.findById(replyId).orElseThrow(EntityNotFoundException::new);
//        System.out.println("reply = " + review);
//    }
}