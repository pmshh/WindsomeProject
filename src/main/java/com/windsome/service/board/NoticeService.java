package com.windsome.service.board;

import com.windsome.constant.Role;
import com.windsome.dto.board.notice.*;
import com.windsome.entity.Account;
import com.windsome.entity.board.Notice;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.board.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AccountRepository accountRepository;

    /**
     * 공지사항 게시판 - 일반 공지사항 조회
     */
    public Page<NoticeListDto> getNoticeList(NoticeSearchDto noticeSearchDto, Pageable pageable) {
        return noticeRepository.getNoticeList(noticeSearchDto, pageable);
    }

    /**
     * 공지사항 게시판 - 상단 고정 공지사항 조회
     */
    public List<Notice> getFixTopNoticeList() {
        return noticeRepository.findAllByNoticeYNOrderByRegTimeDesc(true);
    }

    /**
     * 공지사항 등록
     */
    public void enrollNotice(NoticeDto noticeDto, Account account) {
        Notice notice = Notice.createNotice(noticeDto, account);
        noticeRepository.save(notice);
    }

    /**
     * 공지사항 상세 화면 - 공지사항 조회
     */
    public List<NoticeDtlDto> getNoticeDtl(Long noticeId) {
        return noticeRepository.getNoticeDtl(noticeId)
                .stream()
                .map(n -> new NoticeDtlDto(n, accountRepository.findById(n.getAccountId()).orElseThrow(EntityNotFoundException::new).getName()))
                .collect(Collectors.toList());
    }

    /**
     * 공지사항 수정
     */
    public void updateNotice(Long noticeId, NoticeUpdateDto noticeUpdateDto) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
        notice.updateNotice(noticeUpdateDto);
        noticeRepository.save(notice);
    }

    /**
     * 공지사항 수정 화면 - 공지사항 조회
     */
    public Notice getNotice(Long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 공지사항 삭제
     */
    public void deleteNotice(Long noticeId) {
        noticeRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
        noticeRepository.deleteById(noticeId);
    }

    /**
     * 관리자 권한 검증
     */
    public boolean isAdmin(Account account) {
        return account.getState().equals(Role.ADMIN);
    }

    /**
     * 게시글 삭제
     */
    public void deleteNotices(Long[] noticeIds) {
        for (Long noticeId : noticeIds) {
            Notice notice = noticeRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
            noticeRepository.delete(notice);
        }
    }

    /**
     * 공지글 설정 가능 여부 검증
     */
    public boolean checkNoticeYN(Long noticeId, boolean noticeYn) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
        return notice.isNoticeYN() == noticeYn;
    }

    /**
     * 공지글 설정 수정
     */
    public void updateNoticeYN(Long noticeId, boolean noticeYn) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
        notice.setNoticeYN(noticeYn);
        noticeRepository.save(notice);
    }
}
