package com.helpDesk.ticket.entity;

import com.helpDesk.category.entity.Category;
import com.helpDesk.user.entity.Role;
import com.helpDesk.user.entity.User;
import com.helpDesk.common.BusinessRuleViolationException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ABIERTO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_by_id", nullable = false)
    private Category category;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id", nullable = true)
    private User assignedTo;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Ticket(String title, String description, Type type, Priority priority, User createdBy, Category category) {
        this.title = title.trim();
        this.description = description.trim();
        this.type = type;
        this.priority = priority;
        this.createdBy = createdBy;
        this.category = category;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate () {
        this.updatedAt = LocalDateTime.now();
    }

    public void assignTo (User agent) {
        if (agent == null) {
            throw new IllegalArgumentException("El agente no puede ser nulo");
        }

        if (!agent.isActive()) {
            throw new IllegalArgumentException("No se puede asignar el ticket a un agente desactivado.");
        }

        if (Role.CLIENTE.equals(agent.getRole())) {
            throw new IllegalArgumentException("Solo se puede asignar el ticket a un agente o administrador.");
        }

        if (Status.CERRADO.equals(this.status)) {
            throw new BusinessRuleViolationException("Ticket cerrado. No es posible asignar");
        }

        this.assignedTo = agent;

        if (Status.ABIERTO.equals(this.status)) {
            changeStatus(Status.EN_PROGRESO);
        }
    }

    public void changeStatus (Status newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo");
        }

        if (this.status.equals(newStatus)) {
            throw new IllegalArgumentException("El nuevo estado debe ser diferente al estado actual.");
        }

        switch (this.status) {
            case ABIERTO -> {
                if (Status.EN_PROGRESO.equals(newStatus) || Status.CERRADO.equals(newStatus) || Status.CANCELADO.equals(newStatus)) {
                    this.status = newStatus;
                } else {
                    throw new BusinessRuleViolationException("Desde el estado ABIERTO solo se puede pasar a EN_PROGRESO, CERRADO o CANCELADO.");
                }
            }
            case EN_PROGRESO -> {
                if (Status.RESUELTO.equals(newStatus) || Status.ABIERTO.equals(newStatus) || Status.CERRADO.equals(newStatus)) {
                    this.status = newStatus;
                } else {
                    throw new BusinessRuleViolationException("Transición no válida desde EN_PROGRESO.");
                }
            }
            case RESUELTO -> {
                if (Status.CERRADO.equals(newStatus) || Status.EN_PROGRESO.equals(newStatus)) {
                    this.status = newStatus;
                } else {
                    throw new BusinessRuleViolationException("Transición no válida desde RESUELTO.");
                }
            }
            case CERRADO -> {
                if (Status.EN_PROGRESO.equals(newStatus)) {
                    this.status = newStatus;
                } else {
                    throw new BusinessRuleViolationException("Un ticket cerrado solo puede pasar a EN_PROGRESO de forma excepcional.");
                }
            }
            default -> throw new BusinessRuleViolationException("Estado actual desconocido.");
        }
    }

    public void updateDetails (String title, String description, Type type, Priority priority, Category category) {

        if (this.status == Status.CERRADO) {
            throw new BusinessRuleViolationException("No se puede modificar un ticket cerrado.");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El titulo no puede ser nulo");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descripcion no puede ser nulo");
        }

        if (type == null) {
            throw new IllegalArgumentException("El tipo de ticket no puede ser nulo");
        }

        if (priority == null) {
            throw new IllegalArgumentException("La prioridad no puede ser nulo");
        }

        if (category == null) {
            throw new IllegalArgumentException("La categoria no puede ser nulo");
        }

        this.title = title.trim();
        this.description = description.trim();
        this.type = type;
        this.priority = priority;
        this.category =category;

    }

    public boolean canBeAccessBy(User user) {
        return Role.AGENTE.equals(user.getRole())
                || Role.ADMINISTRADOR.equals(user.getRole())
                || (Role.CLIENTE.equals(user.getRole())
                && user.getId().equals(getCreatedBy().getId()));
    }
}
