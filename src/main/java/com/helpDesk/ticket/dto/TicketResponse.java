package com.helpDesk.ticket.dto;

import java.time.LocalDateTime;

public record TicketResponse (
        Long id,
        String title,
        String description,
        String status,
        String priority,
        String type,
        String categoryName,
        String createdByName,
        LocalDateTime createdAt,
        String assignedToName
){
}
