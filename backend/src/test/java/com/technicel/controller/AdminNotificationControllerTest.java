package com.technicel.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void creatingAListNotifiesAdmin() throws Exception {
        User technician = saveTechnician("NOTE-T1", "NOTENIP1", "secret12");
        String techToken = login(technician.getNip(), "secret12");
        createList(techToken);
        String adminToken = login("ADMIN001", "admin123");

        mockMvc.perform(get("/api/admin/notifications")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.notifications[0].type").value("LIST_CREATED"))
                .andExpect(jsonPath("$.notifications[0].technicianCode").value("NOTE-T1"))
                .andExpect(jsonPath("$.notifications[0].read").value(false))
                .andExpect(jsonPath("$.notifications[0].message").value(org.hamcrest.Matchers.containsString("OBD")));
    }

    @Test
    void adminCanMarkNotificationRead() throws Exception {
        User technician = saveTechnician("NOTE-T2", "NOTENIP2", "secret12");
        String techToken = login(technician.getNip(), "secret12");
        createList(techToken);
        String adminToken = login("ADMIN001", "admin123");

        MvcResult listed = mockMvc.perform(get("/api/admin/notifications")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        long notificationId = objectMapper.readTree(listed.getResponse().getContentAsString())
                .get("notifications")
                .get(0)
                .get("id")
                .asLong();

        mockMvc.perform(post("/api/admin/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void technicianCannotReadAdminNotifications() throws Exception {
        User technician = saveTechnician("NOTE-T3", "NOTENIP3", "secret12");
        String token = login(technician.getNip(), "secret12");

        mockMvc.perform(get("/api/admin/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private void createList(String token) throws Exception {
        mockMvc.perform(post("/api/lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listType": "OBD",
                                  "initialDropCount": 80,
                                  "referenceDate": "2026-09-17"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    private User saveTechnician(String code, String nip, String rawPassword) {
        User user = new User();
        user.setTechnicianCode(code);
        user.setFirstName("Note");
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
