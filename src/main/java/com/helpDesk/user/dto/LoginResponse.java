package com.helpDesk.user.dto;

import com.helpDesk.user.entity.Role;

public record LoginResponse(
        String token,
        String name,
        Role role
) {
}
