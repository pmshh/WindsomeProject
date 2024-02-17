package com.windsome.controller.admin;

import com.windsome.constant.Role;
import com.windsome.dto.account.AccountSearchDto;
import com.windsome.dto.account.AdminPageProfileFormDto;
import com.windsome.exception.AdminDeletionException;
import com.windsome.service.AccountService;
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

    private final AccountService accountService;

    /**
     * 회원 관리 - 회원 목록 조회
     */
    @GetMapping("/members")
    public String getMembers(AccountSearchDto accountSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("accountSearchDto", accountSearchDto);
        model.addAttribute("accountInfoDto", accountService.getAccountInfo(accountSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("maxPage", 10);
        return "admin/account/accountMng";
    }

    /**
     * 회원 관리 - 회원 조회
     */
    @GetMapping("/members/{id}")
    public String getMemberById(@PathVariable("id") Long accountId, Model model) {
        model.addAttribute("viewName", "dtlPage");
        model.addAttribute("profileFormDto", accountService.getAdminPageProfileFormDto(accountId));
        return "admin/account/profileForm";
    }


    /**
     * 회원 관리 - 회원 수정 화면
     */
    @GetMapping("/members/{id}/edit")
    public String showMemberEditForm(@PathVariable("id") Long accountId, Model model) {
        model.addAttribute("viewName", "updatePage");
        model.addAttribute("profileFormDto", accountService.getAdminPageProfileFormDto(accountId));
        return "admin/account/profileForm";
    }

    /**
     * 회원 관리 - 회원 정보 수정
     */
    @PostMapping("/members/{id}")
    public String updateMember(@ModelAttribute("adminPageProfileFormDto") AdminPageProfileFormDto adminPageProfileFormDto, RedirectAttributes redirectAttr) {
        try {
            accountService.updateProfileForAdmin(adminPageProfileFormDto);
            redirectAttr.addFlashAttribute("message", "사용자 정보를 수정했습니다.");
            return "redirect:/admin/members";
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("message", "사용자 정보 수정을 실패했습니다.");
            return "redirect:/admin/members/" + adminPageProfileFormDto.getId() + "/edit";
        }
    }

    /**
     * 회원 관리 - 회원 권한 수정
     */
    @PatchMapping ("/members/{id}")
    public ResponseEntity<String> updateMemberRole(@PathVariable(value = "id") Long accountId, Role role) {
        try {
            accountService.updateAccountRole(accountId, role);
            return ResponseEntity.ok().body("사용자의 권한이 수정되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        }
    }

    /**
     * 회원 관리 - 회원 삭제
     */
    @DeleteMapping ("/members/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable(value = "id") Long accountId) {
        try {
            accountService.deleteAccount(accountId);
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
