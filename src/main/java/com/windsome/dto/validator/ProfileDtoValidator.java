package com.windsome.dto.validator;

import com.windsome.dto.ProfileDto;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ProfileDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ProfileDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProfileDto profileDto = (ProfileDto) target;
        if (!profileDto.getPassword().equals(profileDto.getPasswordConfirm())) {
            errors.rejectValue("newPassword","wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
