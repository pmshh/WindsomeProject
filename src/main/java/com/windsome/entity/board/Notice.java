package com.windsome.entity.board;

import com.windsome.dto.board.notice.NoticeDto;
import com.windsome.dto.board.notice.NoticeUpdateDto;
import com.windsome.entity.Account;
import com.windsome.entity.auditing.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id", callSuper = false)
@Builder @AllArgsConstructor @NoArgsConstructor
@ToString
public class Notice extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String title;

    @Lob
    private String content;

    private boolean noticeYN;

    public static Notice createNotice(NoticeDto noticeDto, Account account) {
        Notice notice = Notice.builder()
                .account(account)
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .noticeYN(noticeDto.noticeYN)
                .build();
        notice.setRegTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        return notice;
    }

    public void updateNotice(NoticeUpdateDto noticeUpdateDto) {
        this.title = noticeUpdateDto.getTitle();
        this.content = noticeUpdateDto.getContent();
        this.noticeYN = noticeUpdateDto.isNoticeYN();
    }
}