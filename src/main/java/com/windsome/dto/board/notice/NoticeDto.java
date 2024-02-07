package com.windsome.dto.board.notice;

import com.windsome.entity.Account;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class NoticeDto {

    public String title;

    public String content;

    public Account account;

    public LocalDateTime regTime;

    public boolean noticeYN;
}
