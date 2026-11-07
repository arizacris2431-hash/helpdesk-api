package com.helpDesk.ticket.service;

import com.helpDesk.category.entity.Category;
import com.helpDesk.category.repository.CategoryRepository;
import com.helpDesk.common.ResourceNotFoundException;
import com.helpDesk.ticket.dto.TicketRequest;
import com.helpDesk.ticket.dto.TicketResponse;
import com.helpDesk.ticket.entity.Ticket;
import com.helpDesk.ticket.repository.TicketRepository;
import com.helpDesk.user.entity.Role;
import com.helpDesk.user.entity.User;
import com.helpDesk.user.exception.BusinessRuleViolationException;
import com.helpDesk.user.exception.ResourceAlreadyExistsException;
import com.helpDesk.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TicketResponse createTicket (TicketRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no se encontro"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("La categoria con ID no se encontro"));


        Ticket ticket = new Ticket(
                request.title(),
                request.description(),
                request.type(),
                request.priority(),
                user,
                category
        );

        Ticket saveTicket = ticketRepository.save(ticket);

        return new TicketResponse(
                saveTicket.getId(),
                saveTicket.getTitle(),
                saveTicket.getDescription(),
                saveTicket.getStatus().name(),
                saveTicket.getPriority().name(),
                saveTicket.getType().name(),
                saveTicket.getCategory().getName(),
                saveTicket.getCreatedBy().getName(),
                saveTicket.getCreatedAt()
        );

    }

    public List<TicketResponse> getAllTickets () {

        return ticketRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public TicketResponse getTicketById (Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceAlreadyExistsException("El usuario no se encontro"));

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El ticket con ID " + id + " no se encontro"));

        if (Role.CLIENTE.equals(user.getRole()) && !user.getId().equals(ticket.getCreatedBy().getId())) {
            throw new ResourceNotFoundException("No es posible mostrar informacion del ticket. No eres dueno de ese ticket");
        }

        return mapToResponse(ticket);

    }


    @Transactional
    public TicketResponse updateTicket (TicketRequest newTicket, Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no se encontro"));

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El ticket con ID  " + id + " no se encontro"));

        Category category = categoryRepository.findById(newTicket.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("La categoria con ID no se encontro"));


        if (Role.CLIENTE.equals(user.getRole()) && !user.getId().equals(ticket.getCreatedBy().getId())) {
            throw new BusinessRuleViolationException("No es posible modificar el ticket. No tiene permisos");
        }

        ticket.updateDetails(newTicket.title(), newTicket.description(), newTicket.type(), newTicket.priority(), category);

        return mapToResponse(ticket);

    }

    public List<TicketResponse> getMyTickets () {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no se encontro"));

        if (!Role.CLIENTE.equals(user.getRole())) {
            throw new BusinessRuleViolationException("Solo los clientes pueden consultar sus propios tickets.");
        }

        return ticketRepository.findByCreatedById(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TicketResponse> getAssignedTickets () {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no se encontro"));

        if (!Role.AGENTE.equals(user.getRole())) {
            throw new BusinessRuleViolationException("Solo los agentes pueden consultar sus propios tickets.");
        }

        return ticketRepository.findByAssignedToId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    private TicketResponse mapToResponse (Ticket ticket) {

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getPriority().name(),
                ticket.getType().name(),
                ticket.getCategory().getName(),      // <-- Aquí es donde explotaba el Lazy
                ticket.getCreatedBy().getName(),     // <-- Aquí también
                ticket.getCreatedAt()
        );
    }

}
