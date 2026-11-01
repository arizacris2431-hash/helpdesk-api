package com.helpDesk.ticket.dto;

import com.helpDesk.ticket.entity.Priority;
import com.helpDesk.ticket.entity.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TicketRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(min = 5, max = 100, message = "El titulo debe tener entre 5 y 100 caracteres")
        String title,

        @NotBlank(message = "La descripcion es obligatorio")
        @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
        String description,

        @NotNull(message = "El tipo de ticket es obligatorio")
        Type type,

        @NotNull(message = "La prioridad del ticket es obligatoria")
        Priority priority,

        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "El ID de la categoria debe ser un valor positivo")
        Long categoryId
) {
}
