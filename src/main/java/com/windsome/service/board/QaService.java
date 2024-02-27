package com.windsome.service.board;

import com.windsome.constant.Role;
import com.windsome.dto.board.qa.*;
import com.windsome.entity.Member;
import com.windsome.entity.board.Qa;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.board.qa.QaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QaService {

    private final QaRepository qaRepository;
    private final MemberRepository memberRepository;
    private final CommentService commentService;

    /**
     * Q&A 게시판 - 게시글 조회
     */
    public Page<QaListDto> getQaList(QaSearchDto qaSearchDto, Pageable pageable) {
        return qaRepository.getQaList(qaSearchDto, pageable);
    }

    /**
     * Q&A 등록
     */
    public void enrollQa(QaEnrollDto qaEnrollDto, Member member) {
        // Dto -> Entity 변환
        Qa qa = Qa.createQa(qaEnrollDto, member);

        // 답글 작성인 경우 뷰에서 originNo 값을 전달
        // originNo 값이 0인 경우 원글 작성, 값이 0 이상인 경우 답글 작성
        if (qaEnrollDto.getOriginNo() == 0) {
            qaRepository.save(qa);
            qa.initReplyInfo(qa.getId(), 0, 0);
        } else {
            Qa findQa = qaRepository.findById(qaEnrollDto.getOriginNo()).orElseThrow(EntityNotFoundException::new);
            qa.initReplyInfo(findQa.getOriginNo(), findQa.getGroupOrd() + 1, findQa.getGroupLayer() + 1);

            // 답글들 중에 원글 groupOrd 보다 큰 값을 가진 경우, 기존 groupOrd 값에 +1 (최신 답글이 제일 위로 올라옴)
            List<Qa> qaList = qaRepository.findByOriginNoAndGroupOrdGreaterThan(findQa.getOriginNo(), findQa.getGroupOrd());
            qaList.forEach(post -> {
                post.setGroupOrd(post.getGroupOrd() + 1);
                qaRepository.save(post);
            });
        }
        qaRepository.save(qa);
    }

    /**
     * 게시글 비밀번호 검증
     */
    public boolean validatePost(Member member, Long qaId, String password) {
        // 관리자 권한 갖고 있을 시 바로 통과
        if (member.getState() == Role.ADMIN) {
            return false;
        }

        // 원글 작성자인 경우 바로 통과
        Qa findQa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        Qa originQa = qaRepository.findById(findQa.getOriginNo()).orElseThrow(EntityNotFoundException::new);
        if (originQa.getMember().getUserIdentifier().equals(member.getUserIdentifier())) {
            return false;
        }

        if (password == null) {
            return true;
        }

        Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        return !password.equals(qa.getPassword());
    }

    /**
     * Q&A 상세 조회(이전 글, 다음 글 포함)
     */
    public List<QaDtlDto> getQaDtl(Long qaId) {
        return qaRepository.getQaDtl(qaId)
                .stream()
                .map(q -> new QaDtlDto(q, memberRepository.findById(q.getMemberId()).orElseThrow(EntityNotFoundException::new).getName()))
                .collect(Collectors.toList());
    }

    /**
     * Q&A 삭제
     */
    public void deleteQa(Long qaId) {
        Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        qaRepository.delete(qa);
    }

    /**
     * 관리자 페이지 - Q&A 선택/전체 삭제
     */
    public void deleteQas(Long[] qaIds) {
        for (Long qaId : qaIds) {
            Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            qaRepository.delete(qa);
        }
    }

    /**
     * 게시글 비밀번호 검증
     */
    public boolean validatePostPassword(Long qaId, String password) {
        Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        return qa.getPassword().equals(password);
    }

    /**
     * Q&A 업데이트
     */
    public void updateQa(QaUpdateDto qaUpdateDto) {
        Qa qa = qaRepository.findById(qaUpdateDto.getQaId()).orElseThrow(EntityNotFoundException::new);
        qa.updateQa(qaUpdateDto);
        qaRepository.save(qa);
    }

    /**
     * Qa 단일 게시글 상세 조회 (for. 게시글 수정)
     */
    public QaUpdateDto getQaForUpdate(Long qaId) {
        Qa qa = qaRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        return QaUpdateDto.createDto(qa);
    }

    /**
     * 댓글 조회
     */
    public List<CommentDto> getCommentList(Long qaId) {
        return commentService.getCommentDtoList(qaId);
    }
}
