package com.windsome.entity.board;

import com.windsome.dto.board.qa.QaEnrollDto;
import com.windsome.dto.board.qa.QaUpdateDto;
import com.windsome.entity.member.Member;
import com.windsome.entity.auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Qa extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qa_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;

    @Lob
    private String content;

    private String password;

    private boolean secretYN; // 비밀 글 설정 여부

    @OneToMany(mappedBy = "qa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    private Long originNo; // 원글 번호
    private int groupOrd; // 원글(답글 포함)에 대한 순서
    private int groupLayer; // 답글 계층

    public static Qa createQa(QaEnrollDto qaEnrollDto, Member member) {
        Qa qa = Qa.builder()
                .member(member)
                .title(qaEnrollDto.getTitle())
                .content(qaEnrollDto.getContent())
                .password(qaEnrollDto.getPassword())
                .secretYN(qaEnrollDto.isSecretYN())
                .originNo(qaEnrollDto.getOriginNo())
                .build();
        qa.setRegTime(LocalDateTime.now());
        qa.setUpdateTime(LocalDateTime.now());
        return qa;
    }

    public void updateQa(QaUpdateDto qaUpdateDto) {
        this.title = qaUpdateDto.getTitle();
        this.content = qaUpdateDto.getContent();
        this.secretYN = qaUpdateDto.isSecretYN();
    }

    public void initReplyInfo(Long originNo, int groupOrd, int groupLayer) {
        this.originNo = originNo;
        this.groupOrd = groupOrd;
        this.groupLayer = groupLayer;
    }
}
