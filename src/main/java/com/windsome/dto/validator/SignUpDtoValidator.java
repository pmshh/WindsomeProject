package com.windsome.dto.validator;

import com.windsome.dto.SignUpFormDto;
import com.windsome.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpDtoValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpFormDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpFormDto signUpFormDto = (SignUpFormDto) target;
        if (accountRepository.existsByUserIdentifier(signUpFormDto.getUserIdentifier())) {
            errors.rejectValue("userId", "invalid.userId", new Object[]{signUpFormDto.getUserIdentifier()}, "이미 사용중인 아이디입니다.");
        }

        if (accountRepository.existsByEmail(signUpFormDto.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpFormDto.getEmail()}, "이미 사용중인 이메일입니다.");
        }
    }
}
