package com.windsome.entity.board;

import com.windsome.dto.board.qa.CommentEnrollDto;
import com.windsome.entity.Account;
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
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "qa_id")
    private Qa qa;

    @Lob
    private String content;

    private boolean secretYN;

    public static Comment toEntity(CommentEnrollDto commentEnrollDto, Qa qa, Account account) {
        return Comment.builder()
                .account(account)
                .qa(qa)
                .content(commentEnrollDto.getContent())
                .secretYN(commentEnrollDto.isSecretYN())
                .build();
    }
}
