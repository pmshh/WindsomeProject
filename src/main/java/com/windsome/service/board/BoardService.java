package com.windsome.service.board;

import com.windsome.constant.Role;
import com.windsome.dto.board.SearchDTO;
import com.windsome.dto.board.notice.*;
import com.windsome.dto.board.qa.*;
import com.windsome.dto.board.review.*;
import com.windsome.entity.board.Board;
import com.windsome.entity.member.Member;
import com.windsome.entity.product.Product;
import com.windsome.entity.product.ProductImage;
import com.windsome.repository.board.BoardRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.product.ProductRepository;
import com.windsome.repository.productImage.ProductImageRepository;
import com.windsome.service.member.MemberService;
import com.windsome.service.product.ProductImageService;
import com.windsome.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    private final MemberService memberService;
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final CommentService commentService;

    /**
     * 공지사항 게시판 - 일반 공지사항 조회
     */
    public Page<NoticeListDTO> getNoticeList(SearchDTO searchDTO, Pageable pageable) {
        return boardRepository.getNoticeList(searchDTO, pageable);
    }

    /**
     * 공지사항 게시판 - 상단 고정 공지사항 조회
     */
    public List<Board> getFixTopNoticeList() {
        return boardRepository.findAllByHasNoticeOrderByRegTimeDesc(true);
    }

    /**
     * 공지사항 등록
     */
    public Long enrollNotice(NoticeDTO noticeDto, Member member) {
        Board board = Board.createNotice(noticeDto, member);
        Board savedBoard = boardRepository.save(board);
        return savedBoard.getId();
    }

    /**
     * 공지사항 상세 화면 - 공지사항 조회
     */
    public List<NoticeDtlDTO> getNoticeDtlList(Long noticeId) {
        List<NoticeDtlDTO> noticeDtlDTOList = new ArrayList<>();
        for (NoticeDtlDtoInterface notice : boardRepository.getNoticeDtl(noticeId)) {
            NoticeDtlDTO noticeDtlDto = new NoticeDtlDTO(notice, memberService.getMemberByMemberId(notice.getMemberId()).getName());
            noticeDtlDTOList.add(noticeDtlDto);
        }
        return noticeDtlDTOList;
    }

    /**
     * 공지사항 수정
     */
    public void updateNotice(Long noticeId, NoticeUpdateDTO noticeUpdateDto) {
        Board board = boardRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
        board.updateNotice(noticeUpdateDto);
        boardRepository.save(board);
    }

    /**
     * 공지사항 수정 화면 - 공지사항 조회
     */
    public Board getNotice(Long noticeId) {
        return boardRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 게시글 삭제 (단건 삭제)
     */
    public void deleteNotice(Long noticeId) {
        boardRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
        boardRepository.deleteById(noticeId);
    }

    /**
     * 관리자 권한 검증
     */
    public boolean isAdmin(Member member) {
        return member.getRole().equals(Role.ADMIN);
    }

    /**
     * 게시글 삭제 (여러건 삭제)
     */
    public void deleteNotices(Long[] noticeIds) {
        for (Long noticeId : noticeIds) {
            Board notice = boardRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
            boardRepository.delete(notice);
        }
    }

    /**
     * 공지글 설정 가능 여부 검증
     */
    public boolean checkNoticeYN(Long noticeId, boolean noticeYn) {
        Board board = boardRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
        return board.isHasNotice() == noticeYn;
    }

    /**
     * 공지글 설정 수정
     */
    public void updateNoticeYN(Long noticeId, boolean noticeYn) {
        Board notice = boardRepository.findById(noticeId).orElseThrow(EntityNotFoundException::new);
        notice.setHasNotice(noticeYn);
        boardRepository.save(notice);
    }

    /**
     * Q&A 게시판 - 게시글 조회
     */
    public Page<QaListDTO> getQaList(SearchDTO searchDTO, Pageable pageable) {
        return boardRepository.getQaList(searchDTO, pageable);
    }

    /**
     * Q&A 등록
     */
    public void enrollQa(QaEnrollDTO qaEnrollDto, Member member) {
        // Dto -> Entity 변환
        Board qa = Board.createQa(qaEnrollDto, member);

        // 답글 작성인 경우 뷰에서 originNo 값을 전달
        // originNo 값이 0인 경우 원글 작성, 값이 0 이상인 경우 답글 작성
        if (qaEnrollDto.getOriginNo() == 0) {
            boardRepository.save(qa);
            qa.initReplyInfo(qa.getId(), 0, 0);
        } else {
            Board findQa = boardRepository.findById(qaEnrollDto.getOriginNo()).orElseThrow(EntityNotFoundException::new);
            qa.initReplyInfo(findQa.getOriginNo(), findQa.getGroupOrder() + 1, findQa.getGroupLayer() + 1);

            // 답글들 중에 원글 groupOrd 보다 큰 값을 가진 경우, 기존 groupOrd 값에 +1 (최신 답글이 제일 위로 올라옴)
            List<Board> qaList = boardRepository.findByOriginNoAndGroupOrderGreaterThan(findQa.getOriginNo(), findQa.getGroupOrder());
            qaList.forEach(post -> {
                post.setGroupOrder(post.getGroupOrder() + 1);
                boardRepository.save(post);
            });
        }
        boardRepository.save(qa);
    }

    /**
     * 게시글 비밀번호 검증
     */
    public boolean validatePost(Member member, Long qaId, String password) {
        // 관리자 권한 갖고 있을 시 바로 통과
        if (member.getRole() == Role.ADMIN) {
            return false;
        }

        // 원글 작성자인 경우 바로 통과
        Board findQa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        Board originQa = boardRepository.findById(findQa.getOriginNo()).orElseThrow(EntityNotFoundException::new);
        if (originQa.getMember().getUserIdentifier().equals(member.getUserIdentifier())) {
            return false;
        }

        if (password == null) {
            return true;
        }

        Board qa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        return !password.equals(qa.getPassword());
    }

    /**
     * Q&A 상세 조회(이전 글, 다음 글 포함)
     */
    public List<QaDtlDTO> getQaDtlList(Long qaId) {
        return boardRepository.getQaDtl(qaId)
                .stream()
                .map(q -> new QaDtlDTO(q, memberService.getMemberByMemberId(q.getMemberId()).getName()))
                .collect(Collectors.toList());
    }

    /**
     * Q&A 삭제
     */
    public void deleteQa(Long qaId) {
        Board qa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        boardRepository.delete(qa);
    }

    /**
     * 관리자 페이지 - Q&A 선택/전체 삭제
     */
    public void deleteQas(Long[] qaIds) {
        for (Long qaId : qaIds) {
            Board qa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
            boardRepository.delete(qa);
        }
    }

    /**
     * 게시글 비밀번호 검증
     */
    public boolean validatePostPassword(Long qaId, String password) {
        Board qa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        return qa.getPassword().equals(password);
    }

    /**
     * Q&A 업데이트
     */
    public void updateQa(QaUpdateDTO qaUpdateDto) {
        Board qa = boardRepository.findById(qaUpdateDto.getQaId()).orElseThrow(EntityNotFoundException::new);
        qa.updateQa(qaUpdateDto);
        boardRepository.save(qa);
    }

    /**
     * Qa 단일 게시글 상세 조회 (for. 게시글 수정)
     */
    public QaUpdateDTO getQaForUpdate(Long qaId) {
        Board qa = boardRepository.findById(qaId).orElseThrow(EntityNotFoundException::new);
        return QaUpdateDTO.createDto(qa);
    }

    /**
     * 댓글 조회
     */
    public List<CommentDTO> getCommentList(Long qaId) {
        return commentService.getCommentDtoList(qaId);
    }

    /**
     * 리뷰 등록
     */
    public void enrollReview(ReviewEnrollDTO reviewEnrollDto, Member member){
        Product product = productService.getProductByProductId(reviewEnrollDto.getProductId());
        Board review = Board.createReview(reviewEnrollDto, product, member);
        boardRepository.save(review);
    }

    /**
     * 리뷰 등록 화면 - 상품 검색(상품 리스트 조회)
     */
    public PageImpl<ProductListDTO> getProductList(ProductSearchDTO searchDto, Pageable pageable) {
        List<ProductListDTO> content = productService.getReviewPageItemList(searchDto.getSearchQuery(), pageable);
        Long count = productService.getReviewPageItemListCount(searchDto.getSearchQuery());
        return new PageImpl<ProductListDTO>(content, pageable, count);
    }

    /**
     * 리뷰 등록 화면 - 상품 상세 화면에서 리뷰 작성 화면 접근 시, 리뷰 등록 화면에 해당 상품 정보 출력
     */
    public ProductDTO getProduct(Long productId) {
        Product product = productService.getProductByProductId(productId);
        String representativeImageUrl = productImageService.getRepresentativeImageUrl(productId, true);
        return ProductDTO.createProductDto(product, representativeImageUrl);
    }

    /**
     * 리뷰 상세 화면 - 리뷰 조회
     */
    public ReviewDtlPageReviewDTO getReviewDtl(Long reviewId) {
        Board review = boardRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        String representativeImageUrl = productImageService.getRepresentativeImageUrl(review.getProduct().getId(), true);
        return ReviewDtlPageReviewDTO.createReviewDtlPageDto(review, representativeImageUrl);
    }

    /**
     * 리뷰 수정
     */
    public void updateReview(ReviewUpdateDTO reviewUpdateDto) {
        Board findReview = boardRepository.findById(reviewUpdateDto.getReviewId()).orElseThrow(EntityNotFoundException::new);
        findReview.updateReview(reviewUpdateDto);
        boardRepository.save(findReview);
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview(Long reviewId) {
        Board review = boardRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        boardRepository.delete(review);
    }

    /**
     * 리뷰 수정/삭제 권한 검증
     */
    public boolean validateReviewOwnership(Long reviewId, Member member) {
        Board review = boardRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        return !StringUtils.equals(member.getUserIdentifier(), review.getMember().getUserIdentifier());
    }

    /**
     * 리뷰 게시판 - 리뷰 조회
     */
    public Page<ReviewListDTO> getReviews(SearchDTO searchDTO, Pageable pageable) {
        return boardRepository.getReviews(searchDTO, pageable);
    }

    /**
     * 상품 상세 화면 - 리뷰 조회
     */
    public Page<ProductReviewDTO> getProductReviewList(Long productId, Pageable pageable) {
        List<Board> content = boardRepository.findByProductIdOrderByIdDesc(productId, pageable);

        List<ProductReviewDTO> productReviewDTOList = new ArrayList<>();
        for (Board review : content) {
            Product product = productService.getProductByProductId(review.getProduct().getId());

            ProductReviewDTO productReviewDTO = ProductReviewDTO.createProductReviewDTO(review, product);
            productReviewDTOList.add(productReviewDTO);
        }
        Long totalCount = boardRepository.countByProductId(productId);

        return new PageImpl<ProductReviewDTO>(productReviewDTOList, pageable, totalCount);
    }

    /**
     * 상품 리뷰 평균 평점
     */
    public void setRatingAvg(Long productId) {
        Product product = productService.getProductByProductId(productId);
        product.setAverageRating(boardRepository.getRatingAvg(productId));
        productService.saveProduct(product);
    }

    /**
     * 리뷰 존재 여부 반환
     */
    public boolean checkExistingReview(Long productId, Long memberId) {
        return boardRepository.existsByProductIdAndMemberId(productId, memberId);
    }

    /**
     * 리뷰 조회수
     */
    public void validateHitsCount(ReviewDtlPageReviewDTO review, HttpServletRequest request, HttpServletResponse response) {
        Board findReview = boardRepository.findById(review.getReviewId()).orElseThrow(EntityNotFoundException::new);

        Cookie[] cookies = Optional.ofNullable(request.getCookies()).orElseGet(() -> new Cookie[0]);

        // "checkedReview" 쿠키가 있을 시, 변수 cookie에 해당 쿠키 추가
        Cookie cookie = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("checkedReview"))
                .findFirst()
                .orElseGet(() -> {
                    findReview.addHitsCount();
                    boardRepository.save(findReview);
                    return new Cookie("checkedReview", "[" + review.getReviewId() + "]");
                });

        // "checkedReview" 쿠키가 없을 시, 조회수 증가 및 "reviewHits" 쿠키 새로 생성
        if (!cookie.getValue().contains("[" + review.getReviewId() + "]")) {
            findReview.addHitsCount();
            boardRepository.save(findReview);
            cookie.setValue(cookie.getValue() + "[" + review.getReviewId() + "]");
        }

        long todayEndSecond = LocalDate.now().atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
        long currentSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge((int) (todayEndSecond - currentSecond)); // 오늘 하루 자정까지 남은 시간초 설정
        response.addCookie(cookie);
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReviews(Long[] reviewIds) {
        for (Long reviewId : reviewIds) {
            Board board = boardRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
            boardRepository.delete(board);
        }
    }
}
