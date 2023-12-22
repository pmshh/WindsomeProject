package com.windsome.dto.validator;

import com.windsome.dto.SignUpDto;
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
        return clazz.isAssignableFrom(SignUpDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpDto signUpDto = (SignUpDto) target;
        if (accountRepository.existsByUserIdentifier(signUpDto.getUserIdentifier())) {
            errors.rejectValue("userId", "invalid.userId", new Object[]{signUpDto.getUserIdentifier()}, "이미 사용중인 아이디입니다.");
        }

        if (accountRepository.existsByEmail(signUpDto.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpDto.getEmail()}, "이미 사용중인 이메일입니다.");
        }
    }
}
