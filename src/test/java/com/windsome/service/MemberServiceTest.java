package com.windsome.service;

import com.windsome.constant.OrderStatus;
import com.windsome.dto.member.MemberFormDTO;
import com.windsome.dto.member.SignUpRequestDTO;
import com.windsome.dto.member.UpdatePasswordDTO;
import com.windsome.dto.member.UserSummaryDTO;
import com.windsome.entity.member.Address;
import com.windsome.entity.member.Member;
import com.windsome.entity.order.Order;
import com.windsome.repository.member.AddressRepository;
import com.windsome.repository.member.MemberRepository;
import com.windsome.repository.order.OrderRepository;
import com.windsome.service.mail.EmailService;
import com.windsome.service.member.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import javax.mail.MessagingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ModelMapper modelMapper;
    @Mock private EmailService emailService;
    @InjectMocks private MemberService memberService;

    @Test
    @DisplayName("회원 가입 테스트")
    void testCreateAccount() {
        // Given
        SignUpRequestDTO signUpRequestDTO = new SignUpRequestDTO();
        signUpRequestDTO.setName("testUser");
        signUpRequestDTO.setPassword("testPassword");

        Member member = new Member();
        member.setName("testUser");
        member.setPassword("testPassword");

        when(modelMapper.map(signUpRequestDTO, Member.class)).thenReturn(member);
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");

        // When
        memberService.createAccount(signUpRequestDTO);

        // Then
        assertEquals(member.getPassword(), "encodedPassword");
        verify(passwordEncoder).encode("testPassword");
        verify(memberRepository).save(member);
    }


    @Test
    @DisplayName("로그인 테스트")
    void testLogin() {
        // Given
        String username = "testUser";
        String password = "testPassword";
        Authentication authentication = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        memberService.login(username, password);

        // Then
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(context, times(1)).setAuthentication(authentication);
    }

    @Test
    @DisplayName("프로필 수정 테스트")
    void testUpdateMember() {
        // Given
        Long memberId = 1L;
        Member member = new Member();
        member.setId(1L);
        MemberFormDTO memberFormDto = new MemberFormDTO();
        memberFormDto.setPassword("newPassword");
        memberFormDto.setName("New Name");
        Address address = new Address();

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        doNothing().when(modelMapper).map(memberFormDto, member);
        when(passwordEncoder.encode(memberFormDto.getPassword())).thenReturn("encodedPassword");
        when(addressRepository.findByMemberIdAndIsDefault(member.getId(), true)).thenReturn(address);

        // When
        memberService.updateMember(memberId, memberFormDto);

        // Then
        verify(modelMapper, times(1)).map(memberFormDto, member);
        verify(passwordEncoder, times(1)).encode(memberFormDto.getPassword());
        verify(addressRepository, times(1)).save(address);
        verify(memberRepository, times(1)).save(member);

        assertEquals("encodedPassword", member.getPassword());
    }

    @Test
    @DisplayName("회원 가입 - 아이디 중복 검사: 중복된 아이디")
    void testCheckDuplicateUserId_DuplicateUserId() {
        // Given
        String userId = "existingUser";
        when(memberRepository.findByUserIdentifier(userId)).thenReturn(new Member());

        // When
        boolean result = memberService.checkDuplicateUserId(userId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("회원 가입 - 아이디 중복 검사: 중복되지 않은 아이디")
    void testCheckDuplicateUserId_NotDuplicateUserId() {
        // Given
        String userId = "newUser";
        when(memberRepository.findByUserIdentifier(userId)).thenReturn(null);

        // When
        boolean result = memberService.checkDuplicateUserId(userId);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("회원 가입/프로필 수정 - 이메일 중복 검사: 중복된 이메일")
    void testCheckDuplicateEmail_DuplicateEmail() {
        // Given
        String email = "existing@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(new Member());

        // When
        boolean result = memberService.checkDuplicateEmail(email);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("회원 가입/프로필 수정 - 이메일 중복 검사: 중복되지 않은 이메일")
    void testCheckDuplicateEmail_NotDuplicateEmail() {
        // Given
        String email = "new@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(null);

        // When
        boolean result = memberService.checkDuplicateEmail(email);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("회원 가입/프로필 수정 - 이메일 인증")
    void testSendVerificationEmail() throws MessagingException {
        // Given
        String email = "test@example.com";

        doNothing().when(emailService).sendEmail(any());

        // When
        String authNum = memberService.sendVerificationEmail(email);

        // Then
        assertNotNull(authNum); // 반환된 인증 번호가 null이 아닌지 확인
        assertEquals(6, authNum.length()); // 반환된 인증 번호의 길이가 6글자인지 확인
        verify(emailService).sendEmail(any()); // EmailService의 sendEmail 메소드가 호출되었는지 확인
    }

    @Test
    @DisplayName("아이디/비밀번호 찾기 - 이메일 인증")
    void testSendEmailVerification() throws MessagingException {
        // Given
        String email = "test@example.com";

        // When
        String authNum = memberService.sendEmail(email);

        // Then
        assertNotNull(authNum); // 반환된 인증 번호가 null이 아닌지 확인
        assertEquals(6, authNum.length()); // 반환된 인증 번호의 길이가 6글자인지 확인
        verify(emailService).sendEmail(any()); // EmailService의 sendEmail 메소드가 호출되었는지 확인
    }

    @Test
    @DisplayName("아이디 찾기 - 회원 아이디 조회")
    void testFindId() {
        // Given
        String name = "test";
        String email = "test@example.com";
        String userIdentifier = "test1234";
        Member member = new Member();
        member.setUserIdentifier("test1234");
        when(memberRepository.findByNameAndEmail(name, email)).thenReturn(java.util.Optional.of(member));

        // When
        String foundUserIdentifier = memberService.findId(name, email);

        // Then
        assertEquals(userIdentifier, foundUserIdentifier);
        verify(memberRepository).findByNameAndEmail(name, email);
    }

    @Test
    @DisplayName("비밀번호 찾기 - 회원 정보 조회")
    void testValidateUserIdentifier() {
        // Given
        String userIdentifier = "user123";
        String name = "John Doe";
        String email = "john@example.com";
        UpdatePasswordDTO updatePasswordDto = new UpdatePasswordDTO(userIdentifier, name, email);
        Member mockMember = Member.builder()
                .userIdentifier(userIdentifier)
                .build();
        when(memberRepository.findByUserIdentifierAndNameAndEmail(userIdentifier, name, email))
                .thenReturn(java.util.Optional.of(mockMember));

        // When
        String foundUserIdentifier = memberService.validateUserIdentifier(updatePasswordDto);

        // Then
        assertEquals(userIdentifier, foundUserIdentifier);
        verify(memberRepository).findByUserIdentifierAndNameAndEmail(userIdentifier, name, email);
    }

    @Test
    @DisplayName("비밀번호 분실 - 비밀번호 초기화")
    void testResetPassword() {
        // Given
        String userIdentifier = "user123";
        String newPassword = "newPassword";
        UpdatePasswordDTO updatePasswordDto = new UpdatePasswordDTO(userIdentifier, "", "", newPassword);
        Member mockMember = Member.builder()
                .userIdentifier(userIdentifier)
                .build();
        when(memberRepository.findByUserIdentifier(userIdentifier)).thenReturn(mockMember);
        when(passwordEncoder.encode(updatePasswordDto.getPassword())).thenReturn(newPassword);

        // When
        memberService.updatePassword(updatePasswordDto);

        // Then
        verify(memberRepository).findByUserIdentifier(userIdentifier);
        verify(passwordEncoder).encode(newPassword);
        verify(memberRepository).save(mockMember);

        assertEquals(mockMember.getPassword(), newPassword);
    }

    @Test
    @DisplayName("마이 페이지 - 회원 정보 조회")
    void testGetUserSummary() {
        // Given
        String userIdentifier = "user123";
        Member mockMember = Member.builder()
                .id(1L)
                .userIdentifier(userIdentifier)
                .build();
        UserSummaryDTO mockUserSummary = UserSummaryDTO.builder()
                .memberId(1L)
                .build();
        when(memberRepository.getUserSummary(userIdentifier)).thenReturn(mockUserSummary);

        // When
        UserSummaryDTO userSummary = memberService.getUserSummary(mockMember);

        // Then
        assertEquals(mockUserSummary, userSummary);
        verify(memberRepository).getUserSummary(userIdentifier);
    }

    @Test
    @DisplayName("마이 페이지 - 회원 총 주문 수 조회")
    void testGetMemberOrderStatusCounts() {
        // Given
        Member member = new Member();
        member.setId(1L);

        List<Order> orderList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderStatus(OrderStatus.PROCESSING);
            orderList.add(order);
        }
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setOrderStatus(OrderStatus.DELIVERED);
            orderList.add(order);
        }

        when(orderRepository.findByMemberId(anyLong())).thenReturn(orderList);

        // When
        Map<String, Integer> memberOrderStatusCounts = memberService.getMemberOrderStatusCounts(member);

        // Then
        assertEquals(5, memberOrderStatusCounts.get("배송준비중"));
        assertEquals(5, memberOrderStatusCounts.get("배송완료"));
        verify(orderRepository).findByMemberId(anyLong());
    }
}