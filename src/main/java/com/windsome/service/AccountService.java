package com.windsome.service;

import com.windsome.dto.MyPageInfoDto;
import com.windsome.dto.ProfileFormDto;
import com.windsome.dto.SignUpFormDto;
import com.windsome.entity.Account;
import com.windsome.constant.Role;
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
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

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

    public void login(String username, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
    }

    public void updateProfile(Account account, ProfileFormDto profileFormDto) {
        modelMapper.map(profileFormDto, account);
        account.setPassword(passwordEncoder.encode(profileFormDto.getPassword()));
        accountRepository.save(account);
    }

    public String findId(String email, String name) {
        Account account = accountRepository.findByEmail(email);
        if (account != null && account.getName().equals(name)) {
            return account.getUserIdentifier();
        } else {
            return "fail";
        }
    }

    public void sendEmailAndUpdatePassword(String email, String name) throws MessagingException {
        UUID uuid = UUID.randomUUID();
        String authNum = uuid.toString().substring(0, 8);
        EmailMessageDto emailMessageDto = EmailMessageDto.builder()
                .to(email)
                .subject("윈섬, 임시 비밀번호 안내")
                .message("안녕하세요. 윈섬 임시비밀번호 안내 관련 이메일 입니다." + "[" + name + "]" +"님의 임시 비밀번호는 "
                        + authNum + " 입니다.")
                .build();
        emailService.sendEmail(emailMessageDto);
        Account account = accountRepository.findByEmail(email);
        account.setPassword(passwordEncoder.encode(authNum));
    }
    public boolean validateEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        return account == null;
    }

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

    public boolean validateId(String userId) {
        Account account = accountRepository.findByUserIdentifier(userId);
        return account == null;
    }

    public boolean checkId(String userId) {
        return !accountRepository.existsByUserIdentifier(userId);
    }

    public boolean userEmailCheck(String email, String name) {
        Account account = accountRepository.findByEmail(email);
        return account != null && account.getName().equals(name);
    }

    public MyPageInfoDto getMyPageInfo(Long accountId) {
        return accountRepository.getMyPageInfo(accountId);
    }
}
