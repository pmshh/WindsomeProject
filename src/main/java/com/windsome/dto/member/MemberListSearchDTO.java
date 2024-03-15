package com.windsome.dto.member;

import com.windsome.constant.Role;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberListSearchDTO {
    private Role searchStateType; // 권한

    private String searchType; // 이름, id, 이메일

    private String searchQuery = "";
}
