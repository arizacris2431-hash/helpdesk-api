package com.helpDesk.user.service;

import com.helpDesk.user.entity.CustomUserDetails;
import com.helpDesk.user.entity.User;
import com.helpDesk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

// Cuando alguien intenta iniciar sesión, Spring Security llama a
// este método automáticamente pasándole el email que escribió el usuario.
//  Este método busca en la base de datos, verifica si existe
//  y le entrega el usuario empaquetado a Spring para que valide si la contraseña es correcta.
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = email.toLowerCase().trim();

        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + normalized));

        return new CustomUserDetails(user);
    }
}
