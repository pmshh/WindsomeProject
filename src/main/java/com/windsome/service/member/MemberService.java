package com.windsome.service.member;

import com.windsome.dto.member.*;
import com.windsome.constant.Role;
import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.repository.member.AddressRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.service.mail.EmailMessageDto;
import com.windsome.service.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AddressRepository addressRepository;

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    /**
     * 회원 조회
     * @return Member
     */
    public Member getMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 회원 저장
     */
    public void saveMember(Member member) {
        memberRepository.save(member);
    }

    /**
     * 회원 가입
     */
    public void createAccount(SignUpRequestDTO signUpRequestDTO) {
        // DTO -> Entity 변환
        Member member = modelMapper.map(signUpRequestDTO, Member.class);
        member.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        member.setRole(Role.USER);

        // 회원가입 시 입력한 주소 정보가 배송지 테이블에 기본 배송지로 저장됨
        addressRepository.save(signUpRequestDTO.toAddress(member, signUpRequestDTO));

        // 회원 저장
        memberRepository.save(member);
    }

    /**
     * 로그인
     */
    public void login(String username, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
    }

    /**
     * 프로필 수정
     */
    public void updateMember(Long memberId, MemberFormDTO memberFormDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        modelMapper.map(memberFormDto, member);
        if (memberFormDto.getPassword() != null) {
            member.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        }

        Optional<Address> address = addressRepository.findByMemberIdAndIsDefault(member.getId(), true);
        if (address.isPresent()) {
            Address findAddress = address.get();
            findAddress.setZipcode(memberFormDto.getZipcode());
            findAddress.setAddr(memberFormDto.getAddr());
            findAddress.setAddrDetail(memberFormDto.getAddrDetail());
            findAddress.setMember(member);
            findAddress.setTel(member.getTel());
            addressRepository.save(findAddress);
        }

        memberRepository.save(member);
    }

    /**
     * 회원 가입 - 아이디 중복 검사
     */
    @Transactional(readOnly = true)
    public boolean checkDuplicateUserId(String userId) {
        return memberRepository.findByUserIdentifier(userId) != null;
    }

    /**
     * 회원 가입/프로필 수정 - 이메일 중복 검사
     */
    @Transactional(readOnly = true)
    public boolean checkDuplicateEmail(String email) {
        return memberRepository.findByEmail(email) != null;
    }

    /**
     * 회원 가입/프로필 수정 - 이메일 인증
     */
    public String sendVerificationEmail(String email) throws MessagingException {
        UUID uuid = UUID.randomUUID();
        String authNum = uuid.toString().substring(0, 6);
        EmailMessageDto emailMessageDto = EmailMessageDto.builder()
                .to(email)
                .subject("윈섬, 회원 가입 인증")
                .message("홈페이지를 방문해주셔서 감사합니다.<br>아래 인증 번호를 인증 번호 확인란에 기입하여 주세요.<br>인증 번호 : " + authNum)
                .build();
        emailService.sendEmail(emailMessageDto);
        return authNum;
    }

    /**
     * 아이디/비밀번호 찾기 - 이메일 인증
     */
    public String sendEmail(String email) throws MessagingException {
        UUID uuid = UUID.randomUUID();
        String authNum = uuid.toString().substring(0, 6);
        EmailMessageDto emailMessageDto = EmailMessageDto.builder().to(email).subject("윈섬, 이메일 인증")
                .message("홈페이지를 방문해주셔서 감사합니다.<br>아래 인증 번호를 인증 번호 확인란에 기입하여 주세요.<br>인증 번호 : " + authNum).build();
        emailService.sendEmail(emailMessageDto);
        return authNum;
    }

    /**
     * 아이디 찾기 - 회원 아이디 조회
     */
    @Transactional(readOnly = true)
    public String findId(String name, String email) {
        return memberRepository.findByNameAndEmail(name, email).orElseThrow(EntityNotFoundException::new).getUserIdentifier();
    }

    /**
     * 비밀번호 찾기 - 회원 정보 조회
     */
    @Transactional(readOnly = true)
    public String validateUserIdentifier(UpdatePasswordDTO updatePasswordDto) {
        return memberRepository.findByUserIdentifierAndNameAndEmail(
                updatePasswordDto.getUserIdentifier(), updatePasswordDto.getName(), updatePasswordDto.getEmail())
                .orElseThrow(EntityNotFoundException::new).getUserIdentifier();
    }

    /**
     * 비밀번호 분실 - 비밀번호 초기화
     */
    public void updatePassword(UpdatePasswordDTO updatePasswordDto) {
        Member member = memberRepository.findByUserIdentifier(updatePasswordDto.getUserIdentifier());
        member.setPassword(passwordEncoder.encode(updatePasswordDto.getPassword()));
        memberRepository.save(member);
    }

    /**
     * 마이 페이지 - 회원 요약 정보 조회
     */
    @Transactional(readOnly = true)
    public UserSummaryDTO getUserSummary(Member member) {
        return memberRepository.getUserSummary(member.getUserIdentifier());
    }

    /**
     * 마이 페이지 - 회원 총 주문 수 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getMemberOrderStatusCounts(Member member) {
        List<Order> orders = orderRepository.findByMemberId(member.getId());

        Map<String, Integer> statusCounts = new HashMap<>();

        // 주문 상태별로 개수를 계산
        for (Order order : orders) {
            String status = order.getOrderStatus().getDisplayName();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }

        return statusCounts;
    }

    /**
     * 마이 페이지 - 총 주문 금액 조회
     */
    public Long getTotalOrderAmount(Member member) {
        return orderRepository.getTotalOrderAmountByMemberId(member.getId());
    }

    /**
     * 회원 상세 조회
     * @return MemberDetailDTO
     */
    public MemberDetailDTO getMemberDetail(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        Address address = addressRepository.findByMemberIdAndIsDefault(member.getId(), true).orElseThrow(EntityNotFoundException::new);
        return MemberDetailDTO.createMemberDetailDTO(member, address);
    }

    /**
     * 관리자 페이지 - 전체 회원 수 조회
     */
    public long getTotalMembers() {
        return memberRepository.count();
    }

    /**
     * 관리자 페이지 - 회원 목록 조회
     */
    public Page<MemberListResponseDTO> getMembersByCriteria(MemberListSearchDTO memberListSearchDto, Pageable pageable) {
        return memberRepository.findMembersByCriteria(memberListSearchDto, pageable);
    }
}
