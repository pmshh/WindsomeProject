package com.windsome.service;

import com.windsome.constant.OrderStatus;
import com.windsome.dto.account.UpdatePasswordDto;
import com.windsome.dto.account.MyPageInfoDto;
import com.windsome.dto.account.ProfileFormDto;
import com.windsome.dto.account.SignUpFormDto;
import com.windsome.entity.Account;
import com.windsome.constant.Role;
import com.windsome.repository.OrderRepository;
import com.windsome.service.mail.EmailMessageDto;
import com.windsome.service.mail.EmailService;
import com.windsome.repository.AccountRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    /**
     * 회원 가입
     */
    public Account saveNewAccount(SignUpFormDto signUpFormDto) {
        Account account = Account.builder()
                .userIdentifier(signUpFormDto.getUserIdentifier())
                .email(signUpFormDto.getEmail())
                .name(signUpFormDto.getName())
                .password(passwordEncoder.encode(signUpFormDto.getPassword()))
                .address1(signUpFormDto.getAddress1())
                .address2(signUpFormDto.getAddress2())
                .address3(signUpFormDto.getAddress3())
                .state(Role.USER)
                .build();
        return accountRepository.save(account);
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
    public void updateProfile(Account account, ProfileFormDto profileFormDto) {
        modelMapper.map(profileFormDto, account);
        account.setPassword(passwordEncoder.encode(profileFormDto.getPassword()));
        accountRepository.save(account);
    }

    /**
     * 회원 가입 - 아이디 중복 검사
     */
    public boolean duplicateCheckId(String userId) {
        Account account = accountRepository.findByUserIdentifier(userId);
        return account == null;
    }

    /**
     * 회원 가입/프로필 수정 - 이메일 중복 검사
     */
    public boolean duplicateCheckEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        return account == null;
    }

    /**
     * 회원 가입/프로필 수정 - 이메일 인증
     */
    public String sendSignUpConfirmEmail(String email) throws MessagingException {
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
        EmailMessageDto emailMessageDto = EmailMessageDto.builder()
                .to(email)
                .subject("윈섬, 이메일 인증")
                .message("홈페이지를 방문해주셔서 감사합니다.<br>아래 인증 번호를 인증 번호 확인란에 기입하여 주세요.<br>인증 번호 : " + authNum)
                .build();
        emailService.sendEmail(emailMessageDto);
        return authNum;
    }

    /**
     * 아이디 찾기 - 회원 아이디 조회
     */
    public String findId(String name, String email) {
        Account account = accountRepository.findByNameAndEmail(name, email);
        if (account != null) {
            return account.getUserIdentifier();
        } else {
            return null;
        }
    }

    /**
     * 비밀번호 찾기 - 회원 정보 조회
     */
    public String validateUserIdentifier(UpdatePasswordDto updatePasswordDto) {
        Account account = accountRepository.findByUserIdentifierAndNameAndEmail(updatePasswordDto.getUserIdentifier(), updatePasswordDto.getName(), updatePasswordDto.getEmail());
        if (account != null) {
            return account.getUserIdentifier();
        } else {
            return null;
        }
    }

    /**
     * 비밀번호 분실 - 비밀번호 초기화
     */
    public void resetPassword(UpdatePasswordDto updatePasswordDto) {
        Account account = accountRepository.findByUserIdentifier(updatePasswordDto.getUserIdentifier());
        account.setPassword(passwordEncoder.encode(updatePasswordDto.getPassword()));
        accountRepository.save(account);
    }

    /**
     * 마이 페이지 - 회원 정보 조회
     */
    public MyPageInfoDto getMyPageInfo(Account account) {
        return accountRepository.getMyPageInfo(account.getUserIdentifier());
    }

    /**
     * 마이 페이지 - 회원 총 주문 수 조회
     */
    public Long getUserOrderCount(Account account) {
        return orderRepository.countByAccountIdAndOrderStatus(account.getId(), OrderStatus.READY);
    }
}
