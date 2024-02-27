package com.windsome.controller.admin;

import com.windsome.constant.Role;
import com.windsome.dto.member.AdminMemberDetailDTO;
import com.windsome.dto.member.MemberListSearchDto;
import com.windsome.exception.AdminDeletionException;
import com.windsome.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
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
    public String getMembers(MemberListSearchDto memberListSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("memberListSearchDto", memberListSearchDto);
        model.addAttribute("memberInfoDto", adminService.getMemberListForAdminPage(memberListSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("maxPage", 10);
        return "admin/member/member-management";
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
    public String updateMember(AdminMemberDetailDTO adminMemberDetailDTO, RedirectAttributes redirectAttr) {
        try {
            adminService.updateMember(adminMemberDetailDTO);
            redirectAttr.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
            return "redirect:/admin/members";
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("message", "회원 정보 수정 도중 오류가 발생하였습니다.");
            return "redirect:/admin/members/" + adminMemberDetailDTO.getId() + "/edit";
        }
    }

    /**
     * 회원 권한 수정
     */
    @PatchMapping ("/members/{memberId}")
    public ResponseEntity<String> updateMemberRole(@PathVariable(value = "memberId") Long memberId, Role role) {
        try {
            adminService.updateMemberRole(memberId, role);
            return ResponseEntity.ok().body("사용자의 권한이 수정되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        }
    }

    /**
     * 회원 삭제
     */
    @DeleteMapping ("/members/{memberId}")
    public ResponseEntity<String> deleteMember(@PathVariable(value = "memberId") Long memberId) {
        try {
            adminService.deleteMember(memberId);
            return ResponseEntity.ok().body("사용자가 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 사용자입니다.");
        } catch (AdminDeletionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 삭제 중 오류 발생");
        }
    }
}
