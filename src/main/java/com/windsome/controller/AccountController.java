package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.dto.ProfileFormDto;
import com.windsome.dto.SignUpFormDto;
import com.windsome.dto.validator.ProfileDtoValidator;
import com.windsome.repository.AccountRepository;
import com.windsome.service.AccountService;
import com.windsome.dto.validator.SignUpDtoValidator;
import com.windsome.entity.Account;
import com.windsome.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final CartService cartService;
    private final SignUpDtoValidator signUpDtoValidator;
    private final ModelMapper modelMapper;

    /**
     * 회원가입 화면
     */
    @GetMapping("/signUp")
    public String signUpForm(@CurrentAccount Account account, Model model) {
        Long cartItemTotalCount = null;
        if (account != null) {
            cartItemTotalCount = cartService.getCartItemTotalCount(account);
        }
        model.addAttribute("cartItemTotalCount", cartItemTotalCount);
        model.addAttribute(SignUpFormDto.builder().build());
        return "account/signUp";
    }

    /**
     * 회원가입
     */
    @PostMapping("/signUp")
    public String signUpSubmit(@Valid SignUpFormDto signUpFormDto, Errors errors) {
        if (errors.hasErrors()) {
            return "account/signUp";
        }

        accountService.saveNewAccount(signUpFormDto);
        accountService.login(signUpFormDto.getUserIdentifier(), signUpFormDto.getPassword());
        return "redirect:/";
    }

    /**
     * 회원정보 수정 화면
     */
    @GetMapping("/account/profile")
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute("cartItemTotalCount", cartService.getCartItemTotalCount(account));
        model.addAttribute(modelMapper.map(account, ProfileFormDto.class));
        return "account/profile";
    }

    /**
     * 회원정보 수정
     */
    @PostMapping("/account/profile")
    public String updateProfile(@CurrentAccount Account account, @Valid ProfileFormDto profileFormDto, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "account/profile";
        }

        accountService.updateProfile(account, profileFormDto);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/account/profile";
    }

    /**
     * 이메일 중복 체크
     */
    @GetMapping("/check/email")
    public ResponseEntity<Object> checkEmail(String email) throws Exception {
        if (!accountService.validateEmail(email)) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일입니다.");
        }
        String authNum = accountService.sendSignUpConfirmEmail(email);
        return ResponseEntity.ok().body(authNum);
    }

    /**
     * 아이디 중복 체크
     */
    @PostMapping("/check/id")
    public ResponseEntity<Object> checkId(String userId) {
        if (!accountService.validateId(userId)) {
            return ResponseEntity.badRequest().body("이미 사용중인 아이디입니다.");
        }
        return ResponseEntity.ok().body(accountService.checkId(userId));
    }

    /**
     * 아이디 찾기 화면
     */
    @GetMapping("/find/id")
    public String findIdGet() {
        return "account/findId";
    }

    /**
     * 아이디 찾기
     */
    @PostMapping("/find/id")
    public ResponseEntity<Object> findIdPost(String email, String name) {
        if (!accountService.userEmailCheck(email, name)) {
            return ResponseEntity.badRequest().body("일치하는 정보가 없습니다.");
        }
        return ResponseEntity.ok().body(accountService.findId(email, name));
    }

    /**
     * 비밀번호 찾기 화면
     */
    @GetMapping("/find/pass")
    public String findPassGet() {
        return "account/findPass";
    }

    /**
     * 비밀번호 찾기
     */
    @PostMapping("/find/pass")
    public ResponseEntity<Object> findPassPost(String email, String name, Model model) throws Exception {
        if (!accountService.userEmailCheck(email, name)) {
            return ResponseEntity.badRequest().body("일치하는 정보가 없습니다.");
        }
        accountService.sendEmailAndUpdatePassword(email, name);
        return ResponseEntity.ok().build();
    }

    /**
     * 마이 페이지 화면
     */
    @GetMapping("/mypage")
    public String mypage(@CurrentAccount Account account, Model model) {
        model.addAttribute("cartItemTotalCount", cartService.getCartItemTotalCount(account));
        model.addAttribute("account", accountRepository.findByUserIdentifier(account.getUserIdentifier()));
        return "account/mypage";
    }

    @InitBinder("signUpForm")
    public void initBinderForSignUpForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpDtoValidator);
    }

    @InitBinder("profileForm")
    public void initBinderForProfileForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new ProfileDtoValidator());
    }
}
