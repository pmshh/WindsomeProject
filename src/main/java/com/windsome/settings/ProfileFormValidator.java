package com.windsome.settings;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ProfileFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ProfileForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProfileForm profileForm = (ProfileForm) target;
        if (!profileForm.getNewPassword().equals(profileForm.getNewPasswordConfirm())) {
            errors.rejectValue("newPassword","wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
