package com.windsome.account;

import com.windsome.account.form.SignUpForm;
import com.windsome.account.validator.SignUpFormValidator;
import com.windsome.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.MessagingException;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
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
        accountService.login(signUpForm.getUserIdentifier(), signUpForm.getPassword());
        return "redirect:/";
    }

    @PostMapping("/check/id")
    @ResponseBody
    public String checkId(String userId) {
        if (accountRepository.existsByUserIdentifier(userId)) {
            return "fail";
        } else {
            return "success";
        }
    }

    @GetMapping("/check/email")
    @ResponseBody
    public String checkEmail(String email) throws Exception {
        return accountService.sendSignUpConfirmEmail(email);
    }

    @GetMapping("/find/pass")
    public String findPassGet() {
        return "account/find-pass";
    }

    @PostMapping("/find/pass")
    public @ResponseBody boolean findPassPost(String email, String name, Model model) throws MessagingException {
        boolean result = accountService.userEmailCheck(email, name);
        if (result) {
            accountService.updatePassword(email, name);
        }
        return result;
    }

    @GetMapping("/find/id")
    public String findIdGet() {
        return "account/find-id";
    }

    @PostMapping("/find/id")
    public @ResponseBody String findIdPost(String email, String name) {
        boolean userEmailCheckResult = accountService.userEmailCheck(email, name);
        return accountService.findId(email, name);
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
