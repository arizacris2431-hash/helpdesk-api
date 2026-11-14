package com.helpDesk.ticket;

import com.helpDesk.category.entity.Category;
import com.helpDesk.category.repository.CategoryRepository;
import com.helpDesk.security.JwtService;
import com.helpDesk.ticket.entity.Priority;
import com.helpDesk.ticket.entity.Ticket;
import com.helpDesk.ticket.entity.Type;
import com.helpDesk.ticket.repository.TicketRepository;
import com.helpDesk.security.CustomUserDetails;
import com.helpDesk.user.entity.Role;
import com.helpDesk.user.entity.User;
import com.helpDesk.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TicketAccessTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private JwtService jwtService;

    private Ticket ticketDeAna;

    @BeforeEach
    void prepararDatos () {
        User ana = userRepository.save(new User("Ana", "ana@test.com", "x", Role.CLIENTE));
        userRepository.save(new User("Luis", "luis@test.com", "X", Role.CLIENTE));
        Category categoria = categoryRepository.save(new Category("Redes"));
        ticketDeAna = ticketRepository.save(new Ticket("Ticket de Ana", "Descripcion de prueba larga",
                Type.INCIDENTE, Priority.ALTA, ana, categoria));

    }

    @Test
    @WithMockUser(username = "luis@test.com", roles = "CLIENTE")
    void luisNoPuedeVerElTicketDeAna () throws Exception {
        mockMvc.perform(get("/api/v1/tickets/" + ticketDeAna.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "ana@test.com", roles = "CLIENTE")
    void anaPideSuTicket () throws Exception {
        mockMvc.perform(get("/api/v1/tickets/" + ticketDeAna.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdByName").value("Ana"));
    }

    @Test
    void tokenDeUsuarioDesactivadoNoFunciona () throws Exception {
        User ana = userRepository.findByEmail("ana@test.com").orElseThrow();
        String token = jwtService.generateToken(new CustomUserDetails(ana));
        ana.deactivate();
        userRepository.save(ana);

        mockMvc.perform(get("/api/v1/tickets/mine")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

    }

    @Test
    @WithMockUser(username = "ana@test.com", roles = "CLIENTE")
    void ticketConCategoriaInexistente () throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"title":"Ticket de prueba",
                         "description":"Descripcion de prueba larga",
                         "type":"INCIDENTE",
                         "priority":"ALTA",
                         "categoryId":999}
                        """
                ))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "fantasma@test.com", roles = "CLIENTE")
    void usuarioInexistenteRecibe404 () throws Exception {
        mockMvc.perform(get("/api/v1/tickets/" + ticketDeAna.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "agente@test.com", roles = "AGENTE")
    void agenteNoPuedeUsarMine () throws Exception{
        userRepository.save(new User("Marta", "agente@test.com", "x", Role.AGENTE));

        mockMvc.perform(get("/api/v1/tickets/mine"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenDevuelveErrorEstandar() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginConContrasenaIncorrecta () throws Exception{
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "ana@test.com",
                            "password": "contrasenaIncorrecta12*"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales Invalidas"));
    }

    @Test
    @WithMockUser(username = "cristian@test.com", roles = "AGENTE")
    void agenteVeSoloSusTickets () throws Exception{
        User user = userRepository.save(new User("Cristian", "cristian@test.com", "x", Role.AGENTE));
        ticketDeAna.assignTo(user);
        mockMvc.perform(get("/api/v1/tickets/assigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ticketDeAna.getId()));
    }

    @Test
    @WithMockUser(username = "ana@test.com", roles = "CLIENTE")
    void clientePideAssigned () throws Exception {
        mockMvc.perform(get("/api/v1/tickets/assigned"))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "marta@test.com", roles = "AGENTE")
    void agenteAsignaTicketAotroAgente () throws Exception {
        userRepository.save(new User("Marta", "marta@test.com", "x", Role.AGENTE));
        User agenteAquienSeAsigna = userRepository.save(new User("Cristian", "cristian@test.com", "x", Role.AGENTE));

        mockMvc.perform(patch("/api/v1/tickets/" + ticketDeAna.getId() + "/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "agentId": %d
                        }
                        """.formatted(agenteAquienSeAsigna.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedToName").value("Cristian"));


    }

    @Test
    @WithMockUser(username = "ana@test.com", roles = "CLIENTE")
    void clienteIntentaAsignar () throws Exception {
        User agenteAquienSeAsigna = userRepository.save(new User("Cristian", "cristian@test.com", "x", Role.AGENTE));

        mockMvc.perform(patch("/api/v1/tickets/" + ticketDeAna.getId() + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "agentId": %d
                        }
                        """.formatted(agenteAquienSeAsigna.getId())))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "marta@test.com", roles = "AGENTE")
    void asignarAunAgenteInexistente () throws Exception {
        userRepository.save(new User("Marta", "marta@test.com", "X", Role.AGENTE));

        mockMvc.perform(patch("/api/v1/tickets/" + ticketDeAna.getId() + "/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "agentId": 999999
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "marta@test.com", roles = "AGENTE")
    void asignarAunCliente () throws Exception {
        userRepository.save(new User("Marta", "marta@test.com", "X", Role.AGENTE));
        User luis = userRepository.findByEmail("luis@test.com").orElseThrow();

        mockMvc.perform(patch("/api/v1/tickets/" + ticketDeAna.getId() + "/assign")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "agentId": %d
                        }
                        """.formatted(luis.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "marta@test.com", roles = "AGENTE")
    void asignarTicketAbiertoLoPasaAenProgreso() throws Exception {
        userRepository.save(new User("Marta", "marta@test.com", "X", Role.AGENTE));
        User userDestino = userRepository.save(new User("Cristian", "cristian@test.com", "x", Role.AGENTE));

        mockMvc.perform(patch("/api/v1/tickets/" + ticketDeAna.getId() + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"agentId": %d}
                            """.formatted(userDestino.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_PROGRESO"));
    }


}
