package com.helpDesk.ticket.repository;


import com.helpDesk.ticket.entity.Priority;
import com.helpDesk.ticket.entity.Status;
import com.helpDesk.ticket.entity.Ticket;
import com.helpDesk.ticket.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedById (Long userId);
    List<Ticket> findByAssignedToId (Long agentId);
    List<Ticket> findByType (Type type);
    List<Ticket> findByPriority (Priority priority);
    List<Ticket> findByStatus (Status status);
}
