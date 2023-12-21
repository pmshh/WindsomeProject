package com.windsome.account;

import com.windsome.account.form.SignUpForm;
import com.windsome.domain.Account;
import com.windsome.account.form.ProfileForm;
import com.windsome.mail.EmailMessage;
import com.windsome.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

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

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
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

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String userIdOrEmail) throws UsernameNotFoundException {
        Account account = accountRepository.findByUserIdentifier(userIdOrEmail);
        if (account == null) {
            account = accountRepository.findByEmail(userIdOrEmail);
        }

        if (account == null) {
            throw new UsernameNotFoundException(userIdOrEmail);
        }

        return new UserAccount(account);
    }
}
