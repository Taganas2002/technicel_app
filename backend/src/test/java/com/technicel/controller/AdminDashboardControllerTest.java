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
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCanLoadDashboardWithTechnicianProgress() throws Exception {
        User technician = saveTechnician("DASH-T1", "DASHNIP1", "secret12");
        String techToken = login(technician.getNip(), "secret12");
        long listId = createList(techToken);
        addProgress(techToken, listId, 25);
        String adminToken = login("ADMIN001", "admin123");

        mockMvc.perform(get("/api/admin/dashboard?search=DASH-T1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalTechnicians").isNumber())
                .andExpect(jsonPath("$.summary.activeLists").isNumber())
                .andExpect(jsonPath("$.technicians.content[0].technicianCode").value("DASH-T1"))
                .andExpect(jsonPath("$.technicians.content[0].fullName").value("Dash Tech"))
                .andExpect(jsonPath("$.technicians.content[0].listType").value("OBD"))
                .andExpect(jsonPath("$.technicians.content[0].completedDrops").value(25))
                .andExpect(jsonPath("$.technicians.content[0].remainingDrops").value(75))
                .andExpect(jsonPath("$.technicians.content[0].status").value("IN_PROGRESS"));
    }

    @Test
    void technicianCannotAccessDashboard() throws Exception {
        User technician = saveTechnician("DASH-T2", "DASHNIP2", "secret12");
        String token = login(technician.getNip(), "secret12");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanRankTechniciansByDayAndMonth() throws Exception {
        User first = saveTechnician("RANK-A1", "RANKNIP1", "secret12");
        User second = saveTechnician("RANK-B1", "RANKNIP2", "secret12");
        String firstToken = login(first.getNip(), "secret12");
        String secondToken = login(second.getNip(), "secret12");
        long firstList = createList(firstToken);
        long secondList = createList(secondToken);
        addProgress(firstToken, firstList, "2026-01-05", 40);
        addProgress(secondToken, secondList, "2026-01-05", 15);
        addProgress(firstToken, firstList, "2026-01-20", 10);
        String adminToken = login("ADMIN001", "admin123");

        mockMvc.perform(get("/api/admin/dashboard/rankings?period=DAY&date=2026-01-05")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("DAY"))
                .andExpect(jsonPath("$.fromDate").value("2026-01-05"))
                .andExpect(jsonPath("$.toDate").value("2026-01-05"))
                .andExpect(jsonPath("$.rows[0].technicianCode").value("RANK-A1"))
                .andExpect(jsonPath("$.rows[0].completedDrops").value(40))
                .andExpect(jsonPath("$.rows[0].rank").value(1))
                .andExpect(jsonPath("$.rows[1].technicianCode").value("RANK-B1"))
                .andExpect(jsonPath("$.rows[1].completedDrops").value(15));

        mockMvc.perform(get("/api/admin/dashboard/rankings?period=MONTH&date=2026-01-05")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("MONTH"))
                .andExpect(jsonPath("$.fromDate").value("2026-01-01"))
                .andExpect(jsonPath("$.toDate").value("2026-01-31"))
                .andExpect(jsonPath("$.rows[0].technicianCode").value("RANK-A1"))
                .andExpect(jsonPath("$.rows[0].completedDrops").value(50))
                .andExpect(jsonPath("$.rows[1].technicianCode").value("RANK-B1"))
                .andExpect(jsonPath("$.rows[1].completedDrops").value(15));
    }

    private long createList(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listType": "OBD",
                                  "initialDropCount": 100,
                                  "referenceDate": "2026-09-17"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void addProgress(String token, long listId, String workDate, int drops) throws Exception {
        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"%s","completedDrops":%s}
                                """.formatted(workDate, drops)))
                .andExpect(status().isCreated());
    }

    private void addProgress(String token, long listId, int drops) throws Exception {
        addProgress(token, listId, "2026-09-17", drops);
    }

    private User saveTechnician(String code, String nip, String rawPassword) {
        User user = new User();
        user.setTechnicianCode(code);
        user.setFirstName("Dash");
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
