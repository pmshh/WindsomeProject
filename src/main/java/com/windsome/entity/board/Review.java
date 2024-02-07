package com.windsome.entity.board;

import com.windsome.dto.board.review.ReviewEnrollDto;
import com.windsome.dto.board.review.ReviewUpdateDto;
import com.windsome.entity.Account;
import com.windsome.entity.Item;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UniqueNumberAndStatus", columnNames={"item_id", "account_id"}) })
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private LocalDateTime regDate;

    private String title;

    private String content;

    @Column(precision =2, scale = 1)
    private BigDecimal rating;

    private int hits;

    private String password;

    public static Review createReview(ReviewEnrollDto reviewEnrollDto, Item item, Account account) {
        Review review = new Review();
        review.setItem(item);
        review.setAccount(account);
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

}
