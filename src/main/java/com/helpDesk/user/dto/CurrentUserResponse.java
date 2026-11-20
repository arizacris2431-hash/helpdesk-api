package com.helpDesk.user.dto;

import com.helpDesk.user.entity.Role;

public record CurrentUserResponse(
        Long id,
        String name,
        String email,
        Role role
) {
}
