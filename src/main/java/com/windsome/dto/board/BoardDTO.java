package com.windsome.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {

    private Long productId;

    private String boardType;

    private boolean hasNotice = false;

    private boolean hasPrivate = false;

    private String password;

    private String title;

    private String content;

    private BigDecimal rating = BigDecimal.valueOf(0);

    private Long originNo = 0L;

    private int groupOrder = 0;

    private int groupLayer = 0;

    private int hits = 0;

}
