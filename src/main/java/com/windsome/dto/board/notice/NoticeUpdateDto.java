package com.windsome.dto.board.notice;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeUpdateDto {

    public String title;

    public String content;

    public boolean noticeYN;

}
