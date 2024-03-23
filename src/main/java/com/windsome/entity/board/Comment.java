package com.windsome.entity.board;

import com.windsome.dto.board.qa.CommentEnrollDTO;
import com.windsome.entity.member.Member;
import com.windsome.entity.auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @Lob
    private String content;

    private boolean hasPrivate; // 비밀 댓글 여부

    public static Comment toEntity(CommentEnrollDTO commentEnrollDto, Board qa, Member member) {
        return Comment.builder()
                .member(member)
                .board(qa)
                .content(commentEnrollDto.getContent())
                .hasPrivate(commentEnrollDto.isHasPrivate())
                .build();
    }
}
