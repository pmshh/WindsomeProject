package com.windsome.account;

import com.windsome.account.form.SignUpForm;
import com.windsome.domain.Account;
import com.windsome.account.form.ProfileForm;
import com.windsome.domain.Role;
import com.windsome.mail.EmailMessage;
import com.windsome.mail.EmailService;
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
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    public Account processNewAccount(SignUpForm signUpForm) {
        return saveNewAccount(signUpForm);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .userIdentifier(signUpForm.getUserIdentifier())
                .email(signUpForm.getEmail())
                .name(signUpForm.getName())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .address1(signUpForm.getAddress1())
                .address2(signUpForm.getAddress2())
                .address3(signUpForm.getAddress3())
                .state(Role.USER)
                .build();
        return accountRepository.save(account);
    }

    public String sendSignUpConfirmEmail(String email) throws MessagingException {
        String authNum = getAuthNum(6);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject("윈섬, 회원 가입 인증")
                .message("홈페이지를 방문해주셔서 감사합니다.<br>아래 인증 번호를 인증 번호 확인란에 기입하여 주세요.<br>인증 번호 : " + authNum)
                .build();
        emailService.sendEmail(emailMessage);
        return authNum;
    }

    public void updateProfile(Account account, ProfileForm profileForm) {
        modelMapper.map(profileForm, account);
        account.setPassword(passwordEncoder.encode(profileForm.getPassword()));
        accountRepository.save(account);
    }

    public void updatePassword(String email, String name) throws MessagingException {
        String authNum = getAuthNum(8);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject("윈섬, 임시 비밀번호 안내")
                .message("안녕하세요. 윈섬 임시비밀번호 안내 관련 이메일 입니다." + "[" + name + "]" +"님의 임시 비밀번호는 "
                        + authNum + " 입니다.")
                .build();
        emailService.sendEmail(emailMessage);
        Account account = accountRepository.findByEmail(email);
        account.setPassword(passwordEncoder.encode(authNum));
    }

    // 회원가입 후 자동 로그인
    public void login(String username, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//                new UserAccount(account),
//                account.getPassword(),
//                List.of(new SimpleGrantedAuthority("ROLE_" + account.getState())));
//        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public boolean userEmailCheck(String email, String name) {
        Account account = accountRepository.findByEmail(email);
        return account != null && account.getName().equals(name);
    }

    private static String getAuthNum(int letterNum) {
        Random random = new Random();
        int createNum = 0;
        String ranNum = "";
        StringBuilder resultNum = new StringBuilder();

        for (int i = 0; i< letterNum; i++) {
            createNum = random.nextInt(9);
            ranNum =  Integer.toString(createNum);
            resultNum.append(ranNum);
        }
        return resultNum.toString();
    }

    public String findId(String email, String name) {
        Account account = accountRepository.findByEmail(email);
        if (account != null && account.getName().equals(name)) {
            return account.getUserIdentifier();
        } else {
            return "fail";
        }
    }
}
