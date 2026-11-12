package com.helpDesk.ticket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignTicketRequest(
        @NotNull(message = "El agente es obligatorio")
        @Positive(message = "El ID del agente debe ser un valor positivo")
        Long agentId
) {
}
