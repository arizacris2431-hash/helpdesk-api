package com.helpDesk.category.entity;

import com.helpDesk.user.exception.BusinessRuleViolationException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    public Category(String name) {
        this.name = name.trim();
    }

    public void deactivate() {
        if (!this.active) {
            throw new BusinessRuleViolationException("La categoria ya se encuentra desactivada");
        }

        this.active = false;
    }

    public void activate () {
        if (this.active) {
            throw new BusinessRuleViolationException("La categoria ya se encuentra activa");
        }

        this.active = true;
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
