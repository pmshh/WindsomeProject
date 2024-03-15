package com.windsome.repository.member;

import com.windsome.dto.member.MemberListResponseDTO;
import com.windsome.dto.member.MemberListSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Page<MemberListResponseDTO> findMembersByCriteria (MemberListSearchDTO memberListSearchDto, Pageable pageable);
}
