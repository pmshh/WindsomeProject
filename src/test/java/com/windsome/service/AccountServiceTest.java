package com.windsome.service;

import com.windsome.WithAccount;
import com.windsome.constant.ItemSellStatus;
import com.windsome.dto.account.MyPageInfoDto;
import com.windsome.dto.account.ProfileFormDto;
import com.windsome.dto.account.SignUpFormDto;
import com.windsome.dto.account.UpdatePasswordDto;
import com.windsome.dto.order.OrderDto;
import com.windsome.dto.order.OrderItemDto;
import com.windsome.entity.Account;
import com.windsome.entity.Item;
import com.windsome.entity.ItemImg;
import com.windsome.repository.AccountRepository;
import com.windsome.repository.ItemImgRepository;
import com.windsome.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


@SpringBootTest
@Transactional
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AccountServiceTest {

    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired OrderService orderService;
    @Autowired ItemRepository itemRepository;
    @Autowired ItemImgRepository itemImgRepository;

    @Test
    @DisplayName("회원 가입 테스트")
    public void saveNewAccount() {
        // given
        SignUpFormDto signUpFormDto = getSignUpFormDto();

        // when
        Account account = accountService.saveNewAccount(signUpFormDto);
        Account savedAccount = accountRepository.findById(account.getId()).orElseThrow(EntityNotFoundException::new);

        // then
        assertNotNull(savedAccount);
        assertEquals(savedAccount.getUserIdentifier(), signUpFormDto.getUserIdentifier());
        assertEquals(savedAccount.getEmail(), signUpFormDto.getEmail());
        assertEquals(savedAccount.getName(), signUpFormDto.getName());
        assertEquals(savedAccount.getAddress1(), signUpFormDto.getAddress1());
        assertEquals(savedAccount.getAddress2(), signUpFormDto.getAddress2());
        assertEquals(savedAccount.getAddress3(), signUpFormDto.getAddress3());
        assertTrue(passwordEncoder.matches(signUpFormDto.getPassword(), savedAccount.getPassword()));
    }

    @Test
    @DisplayName("로그인 테스트")
    public void login() {
        // given
        SignUpFormDto signUpFormDto = getSignUpFormDto();
        accountService.saveNewAccount(signUpFormDto);

        // when
        accountService.login(signUpFormDto.getUserIdentifier(), signUpFormDto.getPassword());

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(authentication.getName(), signUpFormDto.getUserIdentifier());
    }

    @Test
    @DisplayName("프로필 수정 테스트")
    @WithAccount("test1234")
    public void sendSignUpConfirmEmail() throws Exception {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        ProfileFormDto profileFormDto = getProfileFormDto();

        // when
        accountService.updateProfile(account, profileFormDto);

        // then
        assertEquals(account.getUserIdentifier(), profileFormDto.getUserIdentifier());
        assertEquals(account.getEmail(), profileFormDto.getEmail());
        assertEquals(account.getName(), profileFormDto.getName());
        assertEquals(account.getAddress1(), profileFormDto.getAddress1());
        assertEquals(account.getAddress2(), profileFormDto.getAddress2());
        assertEquals(account.getAddress3(), profileFormDto.getAddress3());
        assertTrue(passwordEncoder.matches(profileFormDto.getPassword(), account.getPassword()));
    }

    @Test
    @DisplayName("회원 가입 - 아이디 중복 검사 테스트")
    @WithAccount("test1234")
    public void checkId() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        String userIdentifier = account.getUserIdentifier();

        // when
        boolean result = accountService.duplicateCheckId(userIdentifier);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("회원 가입/프로필 수정 - 이메일 중복 검사 테스트")
    @WithAccount("test1234")
    public void userEmailCheck() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        String email = account.getEmail();

        // when
        boolean result = accountService.duplicateCheckEmail(email);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("아이디 찾기 - 회원 아이디 조회 테스트")
    @WithAccount("test1234")
    public void findId() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");

        // when
        String result = accountService.findId(account.getName(), account.getEmail());

        // then
        assertEquals(result, account.getUserIdentifier());
    }

    @Test
    @DisplayName("비밀번호 찾기 - 회원 정보 조회 테스트")
    @WithAccount("test1234")
    public void validateUserIdentifier() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setUserIdentifier(account.getUserIdentifier());
        updatePasswordDto.setName(account.getName());
        updatePasswordDto.setEmail(account.getEmail());
        // when
        String result = accountService.validateUserIdentifier(updatePasswordDto);

        // then
        assertEquals(result, account.getUserIdentifier());
    }

    @Test
    @DisplayName("비밀번호 분실 - 비밀번호 초기화 테스트")
    @WithAccount("test1234")
    public void resetPassword() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setUserIdentifier(account.getUserIdentifier());
        updatePasswordDto.setPassword("newPassword123@");

        // when
        accountService.resetPassword(updatePasswordDto);

        // then
        Account updatedAccount = accountRepository.findByUserIdentifier("test1234");
        assertTrue(passwordEncoder.matches("newPassword123@", updatedAccount.getPassword()));
    }

    @Test
    @DisplayName("마이 페이지 - 회원 정보 조회 테스트")
    @WithAccount("test1234")
    public void getMyPageInfo() {
        // given
        Account account = accountRepository.findByUserIdentifier("test1234");

        // when
        MyPageInfoDto myPageInfo = accountService.getMyPageInfo(account);

        // then
        assertEquals(myPageInfo.getAccountId(), account.getId());
    }

    @Test
    @DisplayName("마이 페이지 - 회원 총 주문 수 조회 테스트")
    @WithAccount("test1234")
    public void getUserOrderCount() {
        // given
        Item item = saveItem();
        Account account = accountRepository.findByUserIdentifier("test1234");
        OrderDto orderDto = getOrderDto(item);

        // when
        orderService.order(orderDto, account.getUserIdentifier());
        Long userOrderCount = accountService.getUserOrderCount(account);

        // then
        assertEquals(userOrderCount, 1);
    }

    private static ProfileFormDto getProfileFormDto() {
        ProfileFormDto profileFormDto = new ProfileFormDto();
        profileFormDto.setUserIdentifier("수정");
        profileFormDto.setEmail("change@naver.com");
        profileFormDto.setPassword("change1234");
        profileFormDto.setPasswordConfirm("change1234");
        profileFormDto.setAddress1("test1");
        profileFormDto.setAddress2("test2");
        profileFormDto.setAddress3("test3");
        return profileFormDto;
    }

    private static SignUpFormDto getSignUpFormDto() {
        return SignUpFormDto.builder()
                .userIdentifier("test1234")
                .email("test@test.com")
                .name("test")
                .password("test1234")
                .passwordConfirm("test1234")
                .address1("test")
                .address2("test")
                .address3("test")
                .build();
    }

    public Item saveItem() {
        Item item = Item.builder()
                .itemNm("테스트 상품")
                .price(10000)
                .itemDetail("테스트 상품 상세 설명")
                .itemSellStatus(ItemSellStatus.SELL)
                .stockNumber(100)
                .build();
        return itemRepository.save(item);
    }

    private static OrderDto getOrderDto(Item item) {
        List<OrderItemDto> orderItemDtoList = new ArrayList<>();
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setItemId(item.getId());
        orderItemDto.setPrice(item.getPrice());
        orderItemDto.setDiscount(item.getDiscount());
        orderItemDto.setCount(10);
        orderItemDtoList.add(orderItemDto);
        orderItemDto.initPriceAndPoint();

        OrderDto orderDto = new OrderDto("test", "test", "test", "test", "test", "test", orderItemDtoList, 0, 0, 10000, 500, 10000);
        orderDto.initOrderPriceInfo();
        return orderDto;
    }
}