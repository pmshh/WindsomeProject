package com.windsome.controller.member;

import com.windsome.config.security.CurrentMember;
import com.windsome.constant.Role;
import com.windsome.dto.member.MemberFormDTO;
import com.windsome.dto.member.SignUpRequestDTO;
import com.windsome.dto.member.UpdatePasswordDTO;
import com.windsome.dto.validator.ProfileFormDtoValidator;
import com.windsome.entity.member.Member;
import com.windsome.service.member.MemberService;
import com.windsome.dto.validator.SignUpDtoValidator;
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
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final SignUpDtoValidator signUpDtoValidator;
    private final ProfileFormDtoValidator profileFormDtoValidator;
    private final ModelMapper modelMapper;

    /**
     * 회원 등록 화면
     */
    @GetMapping("/members/new")
    public String createMemberForm(Model model) {
        model.addAttribute(SignUpRequestDTO.builder().build());
        return "member/register";
    }

    /**
     * 회원 등록
     */
    @PostMapping("/members/new")
    public String createMember(@Valid SignUpRequestDTO signUpRequestDTO, Errors errors, RedirectAttributes redirectAttr, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("message", "잘못된 접근입니다.");
            return "member/register";
        }

        memberService.createAccount(signUpRequestDTO);
        memberService.login(signUpRequestDTO.getUserIdentifier(), signUpRequestDTO.getPassword());
        redirectAttr.addFlashAttribute("message", "회원가입이 완료되었습니다.");
        return "redirect:/";
    }

    /**
     * 아이디 중복 체크
     */
    @PostMapping("/members/check-userid")
    public ResponseEntity<String> checkDuplicateUserId(String userId) {
        if (memberService.checkDuplicateUserId(userId)) {
            return ResponseEntity.badRequest().body("이미 사용중인 아이디입니다.");
        }
        return ResponseEntity.ok().body("사용 가능한 아이디입니다.");
    }

    /**
     * 이메일 중복 체크 후 이메일 인증
     */
    @GetMapping("/members/email-verification")
    public ResponseEntity<Object> sendVerificationEmailIfAvailable(String email) throws Exception {
        if (memberService.checkDuplicateEmail(email)) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일입니다.");
        }
        return ResponseEntity.ok().body(memberService.sendVerificationEmail(email));
    }

    /**
     * 회원 수정 화면
     */
    @GetMapping("/members/{memberId}/edit")
    public String updateMemberForm(@CurrentMember Member member, Model model) {
        model.addAttribute("member", memberService.getMemberDetail(member.getId()));
        return "member/update-member";
    }

    /**
     * 회원 수정
     */
    @PostMapping("/members/{memberId}/update")
    public String updateMember(@Valid MemberFormDTO memberFormDto, Errors errors, @CurrentMember Member member,
                               Model model, RedirectAttributes attributes) {
        if (member.getRole() != Role.ADMIN) {
            if (errors.hasErrors()) {
                model.addAttribute("member", memberFormDto);
                model.addAttribute("message", "프로필을 수정하는 도중 오류가 발생하였습니다.");
                return "member/update-member";
            }
        }

        memberService.updateMember(member.getId(), memberFormDto);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/members/" + member.getId() + "/edit";
    }

    /**
     * 사용자 계정 찾기 화면
     */
    @GetMapping("/forgot-credentials")
    public String showFindUserAccountForm(String action, Model model) {
        model.addAttribute("action", action);
        return "member/find-account";
    }

    /**
     * 사용자 계정 찾기 화면 - 이메일 인증
     */
    @PostMapping("/forgot-credentials/email-verification")
    public ResponseEntity<String> verifyEmailForIdAndPasswordRecovery(String email) throws MessagingException {
        return ResponseEntity.ok().body(memberService.sendEmail(email));
    }

    /**
     * 아이디 조회 결과 화면 (쿼리 파라미터 숨기기 위한 용도)
     */
    @GetMapping("/forgot-credentials/userid-lookup-result-redirect")
    public String redirectUseridLookupResult(String name, String email, RedirectAttributes redirectAttr) {
        try {
            String userId = memberService.findId(name, email);
            redirectAttr.addFlashAttribute("result", userId);
            return "redirect:/forgot-credentials/userid-lookup-result";
        } catch (EntityNotFoundException e) {
            redirectAttr.addFlashAttribute("result", null);
            return "redirect:/forgot-credentials/userid-lookup-result";
        }
    }

    /**
     * 아이디 조회 결과 화면
     */
    @GetMapping("/forgot-credentials/userid-lookup-result")
    public String showUseridLookupResult() {
        return "member/userid-lookup-result";
    }

    /**
     * 비밀번호 조회 결과 화면 (쿼리 파라미터 숨기기 위한 용도)
     */
    @GetMapping("/forgot-credentials/password-lookup-result-redirect")
    public String redirectPasswordLookupResult(UpdatePasswordDTO updatePasswordDto, RedirectAttributes redirectAttr) {
        try {
            redirectAttr.addFlashAttribute("result", memberService.validateUserIdentifier(updatePasswordDto));
        } catch (EntityNotFoundException e) {
            redirectAttr.addFlashAttribute("result", null);
        }
        return "redirect:/forgot-credentials/password-lookup-result";
    }

    /**
     * 비밀번호 조회 결과 화면
     */
    @GetMapping("/forgot-credentials/password-lookup-result")
    public String showPasswordLookupResult() {
        return "member/password-lookup-result";
    }

    /**
     * 비밀번호 리셋
     */
    @PostMapping("/forgot-credentials/reset-password")
    public ResponseEntity<String> passwordReset(UpdatePasswordDTO updatePasswordDto) {
        memberService.updatePassword(updatePasswordDto);
        return ResponseEntity.ok().body("비밀번호가 변경되었습니다.");
    }

    /**
     * 마이 페이지 화면
     */
    @GetMapping("/mypage")
    public String showMyPage(@CurrentMember Member member, Model model) {
        model.addAttribute("userSummary", memberService.getUserSummary(member));
        model.addAttribute("orderTotalAmount", memberService.getTotalOrderAmount(member));
        model.addAttribute("orderStatusCounts", memberService.getMemberOrderStatusCounts(member));
        return "member/mypage";
    }

    @InitBinder("signUpRequestDTO")
    public void initBinderForSignUpRequestDTO(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpDtoValidator);
    }

    @InitBinder("profileFormDTO")
    public void initBinderForProfileFormDTO(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(profileFormDtoValidator);
    }
}
