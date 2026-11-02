package com.helpDesk.ticket;

import org.hibernate.models.spi.VoidTypeDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TicketSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeListarTodosLosTickets() throws Exception{
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AGENTE")
    void agentePuedeListarTickets () throws Exception{
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk());
    }
}
