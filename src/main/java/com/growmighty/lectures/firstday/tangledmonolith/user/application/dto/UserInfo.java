package com.growmighty.lectures.firstday.tangledmonolith.user.application.dto;

import com.growmighty.lectures.firstday.tangledmonolith.user.domain.User;

public record UserInfo(
        Long id,
        String email,
        String name,
        String phoneNumber
) {
    // User 객체로부터 현재 객체인 UserInfo를 만들겠다.
    public static UserInfo from(User user) {
        return new UserInfo(user.getId(), user.getEmail(), user.getName(), user.getPhoneNumber());
    }
}
