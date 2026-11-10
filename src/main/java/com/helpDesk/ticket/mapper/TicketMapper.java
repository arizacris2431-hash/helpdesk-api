package com.helpDesk.ticket.mapper;

import com.helpDesk.category.entity.Category;
import com.helpDesk.ticket.dto.TicketRequest;
import com.helpDesk.ticket.dto.TicketResponse;
import com.helpDesk.ticket.entity.Ticket;
import com.helpDesk.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse (Ticket ticket) {

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getPriority().name(),
                ticket.getType().name(),
                ticket.getCategory().getName(),
                ticket.getCreatedBy().getName(),
                ticket.getCreatedAt()
        );

    }

    public Ticket toEntity (TicketRequest request, User user, Category category) {

        return new Ticket(
                request.title(),
                request.description(),
                request.type(),
                request.priority(),
                user,
                category
        );
    }
}
