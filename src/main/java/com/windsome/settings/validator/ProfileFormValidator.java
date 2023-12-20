package com.windsome.settings.validator;

import com.windsome.settings.form.ProfileForm;
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
        if (!profileForm.getPassword().equals(profileForm.getPasswordConfirm())) {
            errors.rejectValue("newPassword","wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
