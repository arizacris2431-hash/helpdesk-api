package com.helpDesk.user.service;

import com.helpDesk.user.dto.RegisterRequest;
import com.helpDesk.user.dto.RegisterResponse;
import com.helpDesk.user.entity.Role;
import com.helpDesk.user.entity.User;
import com.helpDesk.common.ResourceAlreadyExistsException;
import com.helpDesk.user.mapper.UserMapper;
import com.helpDesk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public RegisterResponse register (RegisterRequest request) {

        String normalized = request.email().toLowerCase().trim();

        if (userRepository.existsByEmail(normalized)) {
            throw new ResourceAlreadyExistsException("El correo electrónico " + normalized + " ya está registrado por otro usuario.");
        }

        String hashed = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, hashed, Role.CLIENTE);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);

    }


}
