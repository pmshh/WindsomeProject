package com.windsome.dto.validator;

import com.windsome.dto.member.MemberFormDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ProfileFormDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberFormDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberFormDTO memberFormDto = (MemberFormDTO) target;
        if (!memberFormDto.getPassword().equals(memberFormDto.getPasswordConfirm())) {
            errors.rejectValue("newPassword","wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
