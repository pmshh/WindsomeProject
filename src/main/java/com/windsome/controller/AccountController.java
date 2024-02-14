package com.windsome.controller;

import com.windsome.config.security.CurrentAccount;
import com.windsome.constant.Role;
import com.windsome.dto.account.UpdatePasswordDto;
import com.windsome.dto.account.ProfileFormDto;
import com.windsome.dto.account.SignUpFormDto;
import com.windsome.dto.validator.ProfileDtoValidator;
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

import javax.mail.MessagingException;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
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
    public String signUpSubmit(@Valid SignUpFormDto signUpFormDto, RedirectAttributes redirectAttr, Errors errors) {
        if (errors.hasErrors()) {
            return "account/signUp";
        }

        accountService.saveNewAccount(signUpFormDto);
        accountService.login(signUpFormDto.getUserIdentifier(), signUpFormDto.getPassword());
        redirectAttr.addFlashAttribute("message", "회원가입이 완료되었습니다.");
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
        if (errors.hasErrors() && account.getState() == Role.USER) {
            model.addAttribute(account);
            model.addAttribute("message", "프로필을 수정하는 도중 오류가 발생하였습니다.");
            return "account/profile";
        }

        accountService.updateProfile(account, profileFormDto);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/account/profile";
    }

    /**
     * 이메일 중복 체크 후 이메일 인증
     */
    @GetMapping("/check/email")
    public ResponseEntity<Object> confirmEmail(String email) throws Exception {
        if (!accountService.duplicateCheckEmail(email)) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일입니다.");
        }
        String authNum = accountService.sendSignUpConfirmEmail(email);
        return ResponseEntity.ok().body(authNum);
    }

    /**
     * 아이디 중복 체크
     */
    @PostMapping("/check/id")
    public ResponseEntity<Object> duplicateCheckId(String userId) {
        if (!accountService.duplicateCheckId(userId)) {
            return ResponseEntity.badRequest().body("이미 사용중인 아이디입니다.");
        }
        return ResponseEntity.ok().body("사용 가능한 아이디입니다.");
    }

    /**
     * 아이디/비밀번호 찾기 화면
     */
    @GetMapping("/find/account")
    public String findAccount(String type, Model model) {
        model.addAttribute("type", type);
        return "account/findAccount";
    }

    /**
     * 아이디 찾기 화면 (쿼리 파라미터 숨기기 위한 용도)
     */
    @GetMapping("/find/id.do")
    public String findIdRedirect(String name, String email, RedirectAttributes redirectAttr) {
        String result = accountService.findId(name, email);
        redirectAttr.addFlashAttribute("result", result);
        return "redirect:/find/id";
    }

    /**
     * 아이디 찾기 화면
     */
    @GetMapping("/find/id")
    public String findId() {
        return "account/findId";
    }

    /**
     * 비밀번호 변경 화면 (쿼리 파라미터 숨기기 위한 용도)
     */
    @GetMapping("/update/pw.do")
    public String updatePwRedirect(UpdatePasswordDto updatePasswordDto, RedirectAttributes redirectAttr) {
        String result = accountService.validateUserIdentifier(updatePasswordDto);
        redirectAttr.addFlashAttribute("result", result);
        return "redirect:/update/pw";
    }

    /**
     * 비밀번호 변경 화면
     */
    @GetMapping("/update/pw")
    public String updatePw() {
        return "account/updatePw";
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/update/pw")
    public ResponseEntity<String> updatePw(UpdatePasswordDto updatePasswordDto) {
        accountService.resetPassword(updatePasswordDto);
        return ResponseEntity.ok().body("비밀번호가 변경되었습니다.");
    }

    /**
     * 아이디/비밀번호 찾기 화면 - 이메일 인증
     */
    @PostMapping("/find/sendEmail")
    public ResponseEntity<String> findIdPwEmailConfirm(String email) throws MessagingException {
        return ResponseEntity.ok().body(accountService.sendEmail(email));
    }

    /**
     * 마이 페이지 화면
     */
    @GetMapping("/mypage")
    public String mypage(@CurrentAccount Account account, Model model) {
        model.addAttribute("cartItemTotalCount", cartService.getCartItemTotalCount(account));
        model.addAttribute("myPageInfo", accountService.getMyPageInfo(account));
        model.addAttribute("userOrderCount", accountService.getUserOrderCount(account));
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
