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
class DayOffRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void technicianCanCreateDayOffRequest() throws Exception {
        User technician = saveTechnician("DO-T1", "DONIP1", "secret12");
        String token = login(technician.getNip(), "secret12");

        mockMvc.perform(post("/api/day-off-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startDate": "2026-09-20",
                                  "endDate": "2026-09-21",
                                  "reason": "Family"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numberOfDays").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.technicianId").value(technician.getId()));
    }

    @Test
    void startDateAfterEndDateIsRejected() throws Exception {
        User technician = saveTechnician("DO-T2", "DONIP2", "secret12");
        String token = login(technician.getNip(), "secret12");

        mockMvc.perform(post("/api/day-off-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startDate": "2026-09-22",
                                  "endDate": "2026-09-20"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date must be on or before end date"));
    }

    @Test
    void technicianSeesOnlyOwnRequests() throws Exception {
        User first = saveTechnician("DO-T3", "DONIP3", "secret12");
        User second = saveTechnician("DO-T4", "DONIP4", "secret12");
        String firstToken = login(first.getNip(), "secret12");
        String secondToken = login(second.getNip(), "secret12");
        createRequest(firstToken, "2026-09-20", "2026-09-21");
        createRequest(secondToken, "2026-09-22", "2026-09-22");

        mockMvc.perform(get("/api/day-off-requests/my")
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].technicianId").value(first.getId()));
    }

    @Test
    void technicianCannotApproveRequests() throws Exception {
        User technician = saveTechnician("DO-T5", "DONIP5", "secret12");
        String token = login(technician.getNip(), "secret12");
        long id = createRequest(token, "2026-09-20", "2026-09-21");

        mockMvc.perform(patch("/api/admin/day-off-requests/" + id + "/approve")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanApproveAndTechnicianSeesApprovedStatus() throws Exception {
        User technician = saveTechnician("DO-T6", "DONIP6", "secret12");
        String techToken = login(technician.getNip(), "secret12");
        long id = createRequest(techToken, "2026-09-20", "2026-09-21");
        String adminToken = login("ADMIN001", "admin123");

        mockMvc.perform(get("/api/admin/day-off-requests?status=PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").value(org.hamcrest.Matchers.hasItem((int) id)));

        mockMvc.perform(patch("/api/admin/day-off-requests/" + id + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"adminComment": "Approved"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.adminComment").value("Approved"))
                .andExpect(jsonPath("$.processedByName").value("System Admin"))
                .andExpect(jsonPath("$.decisionDate").isNotEmpty());

        mockMvc.perform(get("/api/day-off-requests/my")
                        .header("Authorization", "Bearer " + techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void alreadyProcessedRequestCannotBeProcessedAgain() throws Exception {
        User technician = saveTechnician("DO-T7", "DONIP7", "secret12");
        String techToken = login(technician.getNip(), "secret12");
        long id = createRequest(techToken, "2026-09-20", "2026-09-21");
        String adminToken = login("ADMIN001", "admin123");

        mockMvc.perform(patch("/api/admin/day-off-requests/" + id + "/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"adminComment": "Busy week"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(patch("/api/admin/day-off-requests/" + id + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This day-off request has already been processed"));
    }

    private long createRequest(String token, String start, String end) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/day-off-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startDate": "%s",
                                  "endDate": "%s",
                                  "reason": "Time off"
                                }
                                """.formatted(start, end)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private User saveTechnician(String code, String nip, String rawPassword) {
        User user = new User();
        user.setTechnicianCode(code);
        user.setFirstName("Day");
        user.setLastName("Off");
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
