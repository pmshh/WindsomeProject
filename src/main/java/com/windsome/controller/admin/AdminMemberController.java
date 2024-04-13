package com.windsome.controller.admin;

import com.windsome.constant.Role;
import com.windsome.dto.member.AdminMemberDetailDTO;
import com.windsome.dto.member.AdminMemberFormDTO;
import com.windsome.dto.member.MemberListSearchDTO;
import com.windsome.exception.AdminDeletionException;
import com.windsome.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminMemberController {

    private final AdminService adminService;

    /**
     * 회원 목록 조회
     */
    @GetMapping("/members")
    public String getMembers(MemberListSearchDTO memberListSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("memberInfoDto", adminService.getMemberListForAdminPage(memberListSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("memberListSearchDto", memberListSearchDto);
        model.addAttribute("maxPage", 10);
        return "admin/member/member-management";
    }

    /**
     * 회원 등록
     */
    @GetMapping("/members/new")
    public String showMemberEnrollForm(Model model) {
        model.addAttribute("memberForm", new AdminMemberFormDTO());
        return "admin/member/member-enroll";
    }

    /**
     * 회원 등록
     */
    @PostMapping("/members/new")
    public String createMember(@Valid AdminMemberFormDTO memberFormDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttr) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "회원 등록 도중 오류가 발생하였습니다.");
            model.addAttribute("memberForm", memberFormDTO);
            return "admin/member/member-enroll";
        }

        adminService.enrollMember(memberFormDTO);
        redirectAttr.addFlashAttribute("message", "회원이 등록되었습니다.");
        return "redirect:/admin/members";
    }

    /**
     * 이메일 중복 체크
     */
    @GetMapping("/members/email-verification")
    public ResponseEntity<Object> sendVerificationEmailIfAvailable(String email) throws Exception {
        if (adminService.checkDuplicateEmail(email)) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일입니다.");
        }
        return ResponseEntity.ok().body("사용가능한 이메일입니다.");
    }

    /**
     * 회원 상세 조회
     */
    @GetMapping("/members/{memberId}")
    public String getMemberById(@PathVariable("memberId") Long memberId, Model model) {
        model.addAttribute("viewName", "dtlPage");
        model.addAttribute("memberDetails", adminService.getMemberDetails(memberId));
        return "admin/member/member-form";
    }

    /**
     * 회원 수정 화면
     */
    @GetMapping("/members/{memberId}/edit")
    public String showMemberEditForm(@PathVariable("memberId") Long memberId, Model model) {
        model.addAttribute("viewName", "updatePage");
        model.addAttribute("memberDetails", adminService.getMemberDetails(memberId));
        return "admin/member/member-form";
    }

    /**
     * 회원 수정
     */
    @PostMapping("/members/{memberId}")
    public String updateMember(@PathVariable("memberId") Long memberId, AdminMemberDetailDTO adminMemberDetailDTO, RedirectAttributes redirectAttr) {
        try {
            adminService.updateMember(adminMemberDetailDTO);
            redirectAttr.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
            return "redirect:/admin/members/" + memberId + "/edit";
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("message", "회원 정보 수정 도중 오류가 발생하였습니다.");
            return "redirect:/admin/members/" + memberId + "/edit";
        }
    }

    /**
     * 회원 권한 수정
     */
    @PatchMapping ("/members/{memberId}")
    public ResponseEntity<String> updateMemberRole(@PathVariable(value = "memberId") Long memberId, Role role) {
        try {
            adminService.updateMemberRole(memberId, role);
            return ResponseEntity.ok().body("회원의 권한이 수정되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        }
    }

    /**
     * 회원 삭제
     */
    @DeleteMapping ("/members/delete")
    public ResponseEntity<String> deleteMember(@RequestBody Long[] memberIds) {
        try {
            adminService.deleteMembers(memberIds);
            return ResponseEntity.ok().body("회원이 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        } catch (AdminDeletionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 삭제 도중 오류가 발생하였습니다.");
        }
    }
}
