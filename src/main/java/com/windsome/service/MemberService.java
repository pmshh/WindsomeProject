package com.windsome.service;

import com.windsome.dto.member.*;
import com.windsome.constant.Role;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.service.mail.EmailMessageDto;
import com.windsome.service.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    /**
     * 회원 가입
     */
    public void createAccount(SignUpRequestDTO signUpRequestDTO) {
        Member member = modelMapper.map(signUpRequestDTO, Member.class);
        member.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        member.setRole(Role.USER);
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
    public void updateMember(Member member, MemberFormDTO memberFormDto) {
        modelMapper.map(memberFormDto, member);
        member.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
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
}
