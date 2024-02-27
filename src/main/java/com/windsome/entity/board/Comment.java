package com.windsome.entity.board;

import com.windsome.dto.board.qa.CommentEnrollDto;
import com.windsome.entity.Member;
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
    @JoinColumn(name = "qa_id")
    private Qa qa;

    @Lob
    private String content;

    private boolean secretYN;

    public static Comment toEntity(CommentEnrollDto commentEnrollDto, Qa qa, Member member) {
        return Comment.builder()
                .member(member)
                .qa(qa)
                .content(commentEnrollDto.getContent())
                .secretYN(commentEnrollDto.isSecretYN())
                .build();
    }
}
