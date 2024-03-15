package com.windsome.dto.board.notice;

import com.windsome.entity.member.Member;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDto {

    public String title;

    public String content;

    public Member member;

    public LocalDateTime regTime;

    public boolean noticeYN;
}
