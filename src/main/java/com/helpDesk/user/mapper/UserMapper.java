package com.helpDesk.user.mapper;

import com.helpDesk.user.dto.RegisterRequest;
import com.helpDesk.user.dto.RegisterResponse;
import com.helpDesk.user.entity.Role;
import com.helpDesk.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public RegisterResponse toResponse (User user) {

        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public User toEntity (RegisterRequest request, String hashedPassword, Role role) {

        return new User(
                request.name(),
                request.email(),
                hashedPassword,
                role
        );
    }
}
