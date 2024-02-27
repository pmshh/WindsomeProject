package com.windsome.repository.member;

import com.windsome.dto.member.MemberListResponseDTO;
import com.windsome.dto.member.MemberListSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Page<MemberListResponseDTO> findMembersByCriteria (MemberListSearchDto memberListSearchDto, Pageable pageable);
}
