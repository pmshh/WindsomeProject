package com.windsome.account;

import com.windsome.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.util.Random;


@Controller
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/login")
    public String login() {
        return "account/login";
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @PostMapping("/check-id")
    @ResponseBody
    public String checkId(String userId) {
        if (accountRepository.existsByUserId(userId)) {
            return "fail";
        } else {
            return "success";
        }
    }

    @GetMapping("/send-mail")
    @ResponseBody
    public String sendEmail(String email) {

        System.out.println("email = " + email);
        
        Random random = new Random();
        int checkNum = random.nextInt(888888) + 111111;

        String from = "pms000723@gmail.com";
        String to = email;
        String title = "윈섬, 가입 인증 메일";
        String content = "홈페이지를 방문해주셔서 감사합니다. <br> 인증 번호는 " + checkNum + "입니다. <br> 해당 인증번호를 인증번호 확인란에 기입하여 주세요.";

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String num = Integer.toString(checkNum);
        log.info(num);

        return num;
    }

    @PostMapping("/check-email")
    @ResponseBody
    public String checkEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            return "fail";
        } else {
            return "success";
        }
    }

}
