package com.windsome.entity.board;

import com.windsome.dto.board.review.ReviewEnrollDto;
import com.windsome.dto.board.review.ReviewUpdateDto;
import com.windsome.entity.Member;
import com.windsome.entity.Product;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UniqueNumberAndStatus", columnNames={"product_id", "member_id"}) })
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime regDate; // ToDo - BaseTimeEntity 상속

    private String title;

    private String content;

    @Column(precision =2, scale = 1)
    private BigDecimal rating;

    private int hits;

    private String password;

    // Todo - ModelMapper 적용
    public static Review createReview(ReviewEnrollDto reviewEnrollDto, Product product, Member member) {
        Review review = new Review();
        review.setProduct(product);
        review.setMember(member);
        review.setTitle(reviewEnrollDto.getTitle());
        review.setContent(reviewEnrollDto.getContent());
        review.setPassword(reviewEnrollDto.getPassword());
        review.setRating(reviewEnrollDto.getRating());
        review.setRegDate(LocalDateTime.now());
        review.setHits(0);
        return review;
    }

    public void updateReview(ReviewUpdateDto reviewUpdateDto) {
        this.setTitle(reviewUpdateDto.getTitle());
        this.setContent(reviewUpdateDto.getContent());
        this.setRating(reviewUpdateDto.getRating());
    }

    public void addHitsCount() {
        this.hits += 1;
    }
}
