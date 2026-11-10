package com.helpDesk.ticket.service;

import com.helpDesk.category.entity.Category;
import com.helpDesk.category.repository.CategoryRepository;
import com.helpDesk.common.ForbiddenException;
import com.helpDesk.common.ResourceNotFoundException;
import com.helpDesk.security.CurrentUserService;
import com.helpDesk.ticket.dto.TicketRequest;
import com.helpDesk.ticket.dto.TicketResponse;
import com.helpDesk.ticket.entity.Ticket;
import com.helpDesk.ticket.mapper.TicketMapper;
import com.helpDesk.ticket.repository.TicketRepository;
import com.helpDesk.user.entity.Role;
import com.helpDesk.user.entity.User;
import com.helpDesk.user.exception.BusinessRuleViolationException;
import com.helpDesk.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TicketMapper ticketMapper;
    private final CurrentUserService currentUserService;

    public TicketResponse createTicket (TicketRequest request) {

        User user = currentUserService.getCurrentUser();

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

        return ticketMapper.toResponse(saveTicket);

    }

    public List<TicketResponse> getAllTickets () {

        return ticketRepository.findAll()
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }


    public TicketResponse getTicketById (Long id) {

        User user = currentUserService.getCurrentUser();

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El ticket con ID " + id + " no se encontro"));

        if (Role.CLIENTE.equals(user.getRole()) && !user.getId().equals(ticket.getCreatedBy().getId())) {
            throw new ResourceNotFoundException("No es posible mostrar informacion del ticket. No eres dueno de ese ticket");
        }

        return ticketMapper.toResponse(ticket);

    }


    @Transactional
    public TicketResponse updateTicket (TicketRequest newTicket, Long id) {

        User user = currentUserService.getCurrentUser();

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El ticket con ID  " + id + " no se encontro"));

        Category category = categoryRepository.findById(newTicket.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("La categoria con ID no se encontro"));


        if (Role.CLIENTE.equals(user.getRole()) && !user.getId().equals(ticket.getCreatedBy().getId())) {
            throw new BusinessRuleViolationException("No es posible modificar el ticket. No tiene permisos");
        }

        ticket.updateDetails(newTicket.title(), newTicket.description(), newTicket.type(), newTicket.priority(), category);

        return ticketMapper.toResponse(ticket);

    }

    public List<TicketResponse> getMyTickets () {

        User user = currentUserService.getCurrentUser();

        if (!Role.CLIENTE.equals(user.getRole())) {
            throw new ForbiddenException("Solo los clientes pueden consultar sus propios tickets.");
        }

        return ticketRepository.findByCreatedById(user.getId())
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    public List<TicketResponse> getAssignedTickets () {

        User user = currentUserService.getCurrentUser();

        if (!Role.AGENTE.equals(user.getRole())) {
            throw new BusinessRuleViolationException("Solo los agentes pueden consultar sus propios tickets.");
        }

        return ticketRepository.findByAssignedToId(user.getId())
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }


}
