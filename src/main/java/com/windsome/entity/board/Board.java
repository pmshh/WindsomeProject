package com.windsome.entity.board;

import com.windsome.dto.board.BoardDTO;
import com.windsome.dto.board.qa.QaUpdateDTO;
import com.windsome.dto.board.review.ReviewUpdateDTO;
import com.windsome.entity.auditing.BaseTimeEntity;
import com.windsome.entity.member.Member;
import com.windsome.entity.product.Product;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Board extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(optional = true)
    @JoinColumn(name = "product_id")
    private Product product;

    private String boardType; // 게시판 종류

    private boolean hasNotice; // 공지글 설정 여부

    private boolean hasPrivate; // 비밀글 설정 여부

    private String password; // 비밀번호

    private String title; // 제목

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content; // 내용

    @Column(columnDefinition = "decimal(2,1)")
    private BigDecimal rating; // 평점

    private Long originNo; // 원글 번호

    private int groupOrder; // 원글(답글 포함)에 대한 순서

    private int groupLayer; // 답글 계층

    private int hits; // 조회수

    /**
     * Notice 관련
     */
    public void updateNotice(BoardDTO boardDTO) {
        this.title = boardDTO.getTitle();
        this.content = boardDTO.getContent();
        this.hasNotice = boardDTO.isHasNotice();
    }

    /**
     * Q&A 관련
     */
    public void updateQa(QaUpdateDTO qaUpdateDto) {
        this.title = qaUpdateDto.getTitle();
        this.content = qaUpdateDto.getContent();
        this.hasPrivate = qaUpdateDto.isHasPrivate();
    }

    public void initReplyInfo(Long originNo, int groupOrd, int groupLayer) {
        this.originNo = originNo;
        this.groupOrder = groupOrd;
        this.groupLayer = groupLayer;
    }

    /**
     * Review
     */
    public void updateReview(ReviewUpdateDTO reviewUpdateDto) {
        this.setTitle(reviewUpdateDto.getTitle());
        this.setContent(reviewUpdateDto.getContent());
        this.setRating(reviewUpdateDto.getRating());
    }

    public void addHitsCount() {
        this.hits += 1;
    }
}
