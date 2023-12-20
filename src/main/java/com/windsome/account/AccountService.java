package com.windsome.account;

import com.windsome.domain.Account;
import com.windsome.settings.form.ProfileForm;
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
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final ModelMapper modelMapper;

    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        return newAccount;
    }

    public String sendSignUpConfirmEmail(String email) throws MessagingException {
        Random random = new Random();
        int authNum = random.nextInt(888888) + 111111;

        log.info("{}", authNum);

        String from = "pms000723@gmail.com";
        String title = "윈섬, 회원가입 인증 메일";
        String content = "홈페이지를 방문해주셔서 감사합니다.<br>아래 인증 번호를 인증 번호 확인란에 기입하여 주세요.<br>인증 번호 : " + authNum;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(title);
        helper.setText(content, true);
        javaMailSender.send(message);

        return Integer.toString(authNum);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .userIdentifier(signUpForm.getUserIdentifier())
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .address1(signUpForm.getAddress1())
                .address2(signUpForm.getAddress2())
                .address3(signUpForm.getAddress3())
                .build();
        return accountRepository.save(account);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public void updateProfile(Account account, ProfileForm profileForm) {
        modelMapper.map(profileForm, account);
        account.setPassword(passwordEncoder.encode(profileForm.getPassword()));
        accountRepository.save(account);
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
