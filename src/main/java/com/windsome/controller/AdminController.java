package com.windsome.controller;

import com.windsome.constant.Role;
import com.windsome.dto.account.AccountSearchDto;
import com.windsome.dto.account.AdminPageProfileFormDto;
import com.windsome.dto.admin.PageDto;
import com.windsome.dto.board.notice.NoticeSearchDto;
import com.windsome.dto.board.qa.QaSearchDto;
import com.windsome.dto.board.review.ReviewSearchDto;
import com.windsome.dto.item.ItemFormDto;
import com.windsome.dto.item.ItemSearchDto;
import com.windsome.service.*;
import com.windsome.service.board.NoticeService;
import com.windsome.service.board.QaService;
import com.windsome.service.board.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final CategoryService categoryService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final NoticeService noticeService;
    private final QaService qaService;
    private final ReviewService reviewService;
    private final AccountService accountService;

    /**
     * 메인
     */
    @GetMapping("/main")
    public String home(Model model) {
        Pageable pageable = PageRequest.of(0, 3);
        model.addAttribute("dashboardData", adminService.getDashboardData());
        model.addAttribute("items", itemService.getAdminItemPage(new ItemSearchDto(), pageable));
        model.addAttribute("orders", orderService.getAdminPageOrderList("", pageable));
        model.addAttribute("qaList", qaService.getQaList(new QaSearchDto(), pageable));
        return "admin/main";
    }

    /**
     * 상품 관리 - 조회
     */
    @GetMapping(value = {"/items", "/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page, Model model) {
        model.addAttribute("items", itemService.getAdminItemPage(itemSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 10);
        return "admin/item/itemMng";
    }

    /**
     * 상품 관리 - 상품 등록 화면
     */
    @GetMapping("/item")
    public String saveItemForm(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "admin/item/itemEnroll";
    }

    /**
     * 상품 관리 - 상품 등록
     */
    @PostMapping("/item")
    public String saveItem(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
                           @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) {
        if (bindingResult.hasErrors()) {
            return "admin/item/itemEnroll";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "admin/item/itemEnroll";
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "admin/item/itemEnroll";
        }

        redirectAttributes.addFlashAttribute("save_result", "save_ok");
        return "redirect:/items";
    }

    /**
     * 상품 관리 - 상품 상세 화면
     */
    @GetMapping("/itemDtl/{itemId}")
    public String itemDtl(PageDto pageDto, @PathVariable("itemId") Long itemId, Model model) {
        try {
            model.addAttribute("itemFormDto", itemService.getItemFormDto(itemId));
            model.addAttribute("pageDto", pageDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            model.addAttribute("pageDto", pageDto);
            return "admin/item/itemDtl";
        }
        return "admin/item/itemDtl";
    }

    /**
     * 상품 관리 - 상품 수정 화면
     */
    @GetMapping("/item/{itemId}")
    public String modifyItemForm(PageDto pageDto, @PathVariable("itemId") Long itemId, Model model) {
        try {
            model.addAttribute("itemFormDto", itemService.getItemFormDto(itemId));
            model.addAttribute("pageDto", pageDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            model.addAttribute("pageDto", pageDto);
            return "admin/item/itemUpdate";
        }
        return "admin/item/itemUpdate";
    }

    /**
     * 상품 관리 - 상품 수정
     */
    @PostMapping("/item/{itemId}")
    public String modifyItem(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, RedirectAttributes redirectAttributes,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) {
        if (bindingResult.hasErrors()) {
            return "admin/item/itemUpdate";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            redirectAttributes.addFlashAttribute("update_result", "required_rep_img");
            return "redirect:/items";
        }

        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("update_result", "update_fail");
            return "redirect:/items";
        }

        redirectAttributes.addFlashAttribute("update_result", "update_ok");
        return "redirect:/items";
    }

    /**
     * 상품 관리 - 상품 삭제
     */
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable("itemId") Long itemId) {
        try {
            itemService.deleteItem(itemId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("일치하는 상품 정보가 없습니다.");
        }
        return ResponseEntity.ok().body(itemId);
    }

    /**
     * 상품 관리 - 카테고리 조회
     */
    @GetMapping("/item/categories")
    public ResponseEntity<String> getItemCategories() throws Exception {
        return ResponseEntity.ok().body(categoryService.getJsonCategories());
    }

    /**
     * 주문 관리 - 조회
     */
    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderManage(String userIdentifier, @PathVariable("page") Optional<Integer> page, Model model) {
        model.addAttribute("orders", orderService.getAdminPageOrderList(userIdentifier == null ? "" : userIdentifier, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("maxPage", 10);
        return "admin/order/orderMng";
    }

    /**
     * 주문 관리 - 주문 취소
     */
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId) {
        try {
            orderService.cancelOrder(orderId);
        } catch (EntityNotFoundException e) {
            ResponseEntity.badRequest().body("주문 취소 도중 오류가 발생하였습니다.");
        }
        return ResponseEntity.ok().body("주문이 취소되었습니다.");
    }

    /**
     * 회원 관리 - 회원 조회
     */
    @GetMapping("/account")
    public String userManage(AccountSearchDto accountSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("accountSearchDto", accountSearchDto);
        model.addAttribute("accountInfoDto", accountService.getAccountInfo(accountSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("maxPage", 10);
        return "admin/account/accountMng";
    }

    /**
     * 회원 관리 - 회원 정보 상세 화면
     */
    @GetMapping("/account/{accountId}")
    public String accountDtl(@PathVariable("accountId") Long accountId, Model model) {
        model.addAttribute("viewName", "dtlPage");
        model.addAttribute("profileFormDto", accountService.getAdminPageProfileFormDto(accountId));
        return "admin/account/profileForm";
    }


    /**
     * 회원 관리 - 회원 정보 수정 화면
     */
    @GetMapping("/account/update/{accountId}")
    public String updateAccountForm(@PathVariable("accountId") Long accountId, Model model) {
        model.addAttribute("viewName", "updatePage");
        model.addAttribute("profileFormDto", accountService.getAdminPageProfileFormDto(accountId));
        return "admin/account/profileForm";
    }

    /**
     * 회원 관리 - 회원 정보 수정
     */
    @PostMapping ("/account/update/{accountId}")
    public String updateAccount(AdminPageProfileFormDto adminPageProfileFormDto, RedirectAttributes redirectAttr, Model model) {
        try {
            accountService.updateProfileForAdmin(adminPageProfileFormDto);
        } catch (Exception e) {
            model.addAttribute("profileFormDto", accountService.getAdminPageProfileFormDto(adminPageProfileFormDto.getId()));
            model.addAttribute("message", "회원 정보 수정을 실패했습니다.");
            return "admin/account/profileForm";
        }
        redirectAttr.addFlashAttribute("message", "회원 정보를 수정했습니다.");
        return "redirect:/admin/account";
    }

    /**
     * 회원 관리 - 회원 권한 수정
     */
    @PatchMapping ("/account/update/{accountId}")
    public ResponseEntity<String> updateAccountRole(@PathVariable Long accountId, Role role) {
        try {
            accountService.updateAccountRole(accountId, role);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        }
        return ResponseEntity.ok().body("회원의 권한이 수정되었습니다.");
    }

    /**
     * 회원 관리 - 회원 삭제
     */
    @DeleteMapping ("/account/delete/{accountId}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long accountId) {
        try {
            accountService.deleteAccount(accountId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        }
        return ResponseEntity.ok().body("회원이 삭제되었습니다.");
    }

    /**
     * 게시판 관리(Notice) - 조회
     */
    @GetMapping("/board/notice")
    public String noticeManage(NoticeSearchDto noticeSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("noticeSearchDto", noticeSearchDto);
        model.addAttribute("noticeList", noticeService.getNoticeList(noticeSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("fixTopNoticeList", noticeService.getFixTopNoticeList());
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "admin/board/noticeMng";
    }

    /**
     * 게시판 관리(Notice) - 게시글 삭제
     */
    @DeleteMapping("/board/notice")
    public ResponseEntity<String> deleteNotice(@RequestParam(value = "noticeIds") Long[] noticeIds) {
        try {
            noticeService.deleteNotices(noticeIds);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
        return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
    }

    /**
     * 게시판 관리(Notice) - 게시글 수정
     */
    @PatchMapping("/board/notice/{noticeId}")
    public ResponseEntity<String> updateNotice(Long noticeId, boolean noticeYn) {
        if (noticeService.checkNoticeYN(noticeId, noticeYn)) {
            return ResponseEntity.badRequest().body("이미 공지글로 설정되어있습니다.");
        }

        try {
            noticeService.updateNoticeYN(noticeId, noticeYn);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 게시판 관리(Q&A) - 조회
     */
    @GetMapping("/board/qa")
    public String qaManage(QaSearchDto qaSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("qaList", qaService.getQaList(qaSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("qaSearchDto", qaSearchDto);
        model.addAttribute("maxPage", 10);
        model.addAttribute("page", page.orElse(0));
        return "admin/board/qaMng";
    }

    /**
     * 게시판 관리(Q&A) - 게시글 삭제
     */
    @DeleteMapping("/board/qa")
    public ResponseEntity<String> deleteQa(Long[] qaIds) {
        try {
            qaService.deleteQas(qaIds);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
        return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
    }

    /**
     * 게시판 관리(Review) - 조회
     */
    @GetMapping("/board/review")
    public String reviewManage(ReviewSearchDto reviewSearchDto, Optional<Integer> page, Model model) {
        model.addAttribute("reviews", reviewService.getReviews(reviewSearchDto, PageRequest.of(page.orElse(0), 10)));
        model.addAttribute("reviewSearchDto", reviewSearchDto);
        model.addAttribute("maxPage", 10);
        return "admin/board/reviewMng";
    }

    /**
     * 게시판 관리(Review) - 게시글 삭제
     */
    @DeleteMapping("/board/review")
    public ResponseEntity<String> deleteReview(Long[] reviewIds) {
        try {
            reviewService.deleteReviews(reviewIds);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 게시글입니다.");
        }
        return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
    }
}

