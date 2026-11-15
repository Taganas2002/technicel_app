package com.technicel.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technicel.entity.Role;
import com.technicel.entity.User;
import com.technicel.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AdminTechnicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCanCreateTechnician() throws Exception {
        String token = adminToken();

        mockMvc.perform(post("/api/admin/technicians")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technicianCode": "TECH-001",
                                  "firstName": "John",
                                  "lastName": "Smith",
                                  "nip": "NIP001",
                                  "password": "secret12",
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.technicianCode").value("TECH-001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.nip").value("NIP001"))
                .andExpect(jsonPath("$.role").value("TECHNICIAN"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void duplicateNipIsRejected() throws Exception {
        String token = adminToken();
        createTechnician(token, "TECH-101", "DUPNIP1");

        mockMvc.perform(post("/api/admin/technicians")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technicianCode": "TECH-102",
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "nip": "DUPNIP1",
                                  "password": "secret12",
                                  "active": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("NIP is already in use"));
    }

    @Test
    void duplicateTechnicianCodeIsRejected() throws Exception {
        String token = adminToken();
        createTechnician(token, "TECH-201", "NIP201");

        mockMvc.perform(post("/api/admin/technicians")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technicianCode": "TECH-201",
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "nip": "NIP202",
                                  "password": "secret12",
                                  "active": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Technician code is already in use"));
    }

    @Test
    void adminCanRetrieveTechnician() throws Exception {
        String token = adminToken();
        MvcResult created = createTechnician(token, "TECH-301", "NIP301");
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/admin/technicians/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianCode").value("TECH-301"))
                .andExpect(jsonPath("$.nip").value("NIP301"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void adminCanDeactivateTechnician() throws Exception {
        String token = adminToken();
        MvcResult created = createTechnician(token, "TECH-401", "NIP401");
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/admin/technicians/" + id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void technicianIsForbiddenFromAdminTechnicianEndpoints() throws Exception {
        User technician = saveTechnician("TECH-501", "NIP501", "secret12");
        String token = login(technician.getNip(), "secret12");

        mockMvc.perform(get("/api/admin/technicians")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/technicians")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technicianCode": "TECH-502",
                                  "firstName": "Nope",
                                  "lastName": "Nope",
                                  "nip": "NIP502",
                                  "password": "secret12",
                                  "active": true
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private String adminToken() throws Exception {
        return login("ADMIN001", "admin123");
    }

    private MvcResult createTechnician(String token, String code, String nip) throws Exception {
        return mockMvc.perform(post("/api/admin/technicians")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technicianCode": "%s",
                                  "firstName": "John",
                                  "lastName": "Smith",
                                  "nip": "%s",
                                  "password": "secret12",
                                  "active": true
                                }
                                """.formatted(code, nip)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private User saveTechnician(String code, String nip, String rawPassword) {
        User user = new User();
        user.setTechnicianCode(code);
        user.setFirstName("Test");
        user.setLastName("Tech");
        user.setNip(nip);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.TECHNICIAN);
        user.setActive(true);
        return userRepository.saveAndFlush(user);
    }

    private String login(String nip, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nip":"%s","password":"%s"}
                                """.formatted(nip, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
