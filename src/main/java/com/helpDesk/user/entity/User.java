package com.helpDesk.user.entity;

import com.helpDesk.common.BusinessRuleViolationException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User(String name, String email, String password, Role role) {
        this.name = name.trim();
        this.email = email.toLowerCase().trim();
        this.password = password;
        this.role = role;
    }

    @PrePersist
    protected void onCreate () {
        this.createdAt = LocalDateTime.now();
    }

    public void activate () {

        if (this.active) {
            throw new BusinessRuleViolationException("El usuario ya se encuentra activo.");
        }
        this.active = true;
    }

    public void deactivate () {

        if (!this.active) {
            throw new BusinessRuleViolationException("El usuario ya se encuentra inactivo.");
        }
        this.active = false;
    }

    public void changeEmail (String newEmail) {

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (newEmail == null || newEmail.isEmpty()) {
            throw new IllegalArgumentException("El nuevo email no puede estar vacío.");
        }

        String normalized = newEmail.toLowerCase().trim();

        if (!normalized.matches(emailRegex)) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
        }

        if (this.email.equals(normalized)) {
            throw new IllegalArgumentException("El nuevo email debe ser diferente al email actual");
        }

        this.email = normalized;
    }

    public void changeName (String newName) {

        if (newName == null || newName.isEmpty()) {
            throw new IllegalArgumentException("El nuevo nombre no puede estar vacío");
        }

        String normalized = newName.trim();

        if (this.name.equals(normalized)) {
            throw new IllegalArgumentException("El nuevo nombre no puede ser igual al actual.");
        }

        this.name = normalized;
    }
}
