package com.windsome.dto.validator;

import com.windsome.dto.member.SignUpRequestDTO;
import com.windsome.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpDtoValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpRequestDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpRequestDTO signUpRequestDTO = (SignUpRequestDTO) target;
        if (memberRepository.existsByUserIdentifier(signUpRequestDTO.getUserIdentifier())) {
            errors.rejectValue("userIdentifier", "invalid.userId", new Object[]{signUpRequestDTO.getUserIdentifier()}, "이미 사용중인 아이디입니다.");
        }

        if (memberRepository.existsByEmail(signUpRequestDTO.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpRequestDTO.getEmail()}, "이미 사용중인 이메일입니다.");
        }
    }
}
