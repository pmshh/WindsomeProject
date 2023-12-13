package com.windsome.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ROLE_USER, ROLE_ANONYMOUS, ROLE_ADMIN;
}
