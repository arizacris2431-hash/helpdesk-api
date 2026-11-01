package com.helpDesk.ticket.controller;


import com.helpDesk.ticket.dto.TicketRequest;
import com.helpDesk.ticket.dto.TicketResponse;
import com.helpDesk.ticket.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
@Validated
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket (@Valid @RequestBody TicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<TicketResponse>> getMyTickets() {
        return ResponseEntity.ok(ticketService.getMyTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById (@PathVariable @Positive Long id){
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

}
