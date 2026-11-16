package com.helpDesk.ticket.dto;

import com.helpDesk.ticket.entity.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest (

        @NotNull(message = "El estado es obligatorio")
        Status status
){
}
