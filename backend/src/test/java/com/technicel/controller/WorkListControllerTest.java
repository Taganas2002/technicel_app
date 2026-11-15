package com.technicel.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technicel.entity.DailyProgress;
import com.technicel.entity.Role;
import com.technicel.entity.User;
import com.technicel.entity.WorkList;
import com.technicel.repository.DailyProgressRepository;
import com.technicel.repository.UserRepository;
import com.technicel.repository.WorkListRepository;
import java.time.LocalDate;
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
class WorkListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkListRepository workListRepository;

    @Autowired
    private DailyProgressRepository dailyProgressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void technicianCanCreateOwnWorkList() throws Exception {
        User technician = saveTechnician("WL-T1", "WLNIP1", "secret12");
        String token = login(technician.getNip(), "secret12");

        mockMvc.perform(post("/api/lists")
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
                .andExpect(jsonPath("$.listType").value("OBD"))
                .andExpect(jsonPath("$.initialDropCount").value(100))
                .andExpect(jsonPath("$.totalCompleted").value(0))
                .andExpect(jsonPath("$.remaining").value(100))
                .andExpect(jsonPath("$.progressPercentage").value(0.0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.technicianId").value(technician.getId()));
    }

    @Test
    void technicianCannotCreateSecondInProgressList() throws Exception {
        User technician = saveTechnician("WL-T2", "WLNIP2", "secret12");
        String token = login(technician.getNip(), "secret12");
        createList(token);

        mockMvc.perform(post("/api/lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listType": "DPO",
                                  "initialDropCount": 50,
                                  "referenceDate": "2026-09-18"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void technicianCannotAccessAnotherTechniciansList() throws Exception {
        User first = saveTechnician("WL-T3", "WLNIP3", "secret12");
        User second = saveTechnician("WL-T4", "WLNIP4", "secret12");
        String firstToken = login(first.getNip(), "secret12");
        String secondToken = login(second.getNip(), "secret12");
        long listId = createList(firstToken);

        mockMvc.perform(get("/api/lists/" + listId)
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianCannotChangeInitialDrops() throws Exception {
        User technician = saveTechnician("WL-T5", "WLNIP5", "secret12");
        String token = login(technician.getNip(), "secret12");
        long listId = createList(token);

        mockMvc.perform(patch("/api/admin/lists/" + listId + "/initial-drops")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"initialDropCount": 80}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListTechnicianListsAndChangeInitialDrops() throws Exception {
        User technician = saveTechnician("WL-T6", "WLNIP6", "secret12");
        String techToken = login(technician.getNip(), "secret12");
        long listId = createList(techToken);
        addProgress(listId, technician, LocalDate.of(2026, 9, 17), 60);

        String adminToken = login("ADMIN001", "admin123");
        mockMvc.perform(get("/api/admin/technicians/" + technician.getId() + "/lists")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(listId))
                .andExpect(jsonPath("$[0].totalCompleted").value(60))
                .andExpect(jsonPath("$[0].remaining").value(40));

        mockMvc.perform(patch("/api/admin/lists/" + listId + "/initial-drops")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"initialDropCount": 50}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialDropCount").value(50))
                .andExpect(jsonPath("$.remaining").value(0))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.completionDate").isNotEmpty());

        mockMvc.perform(patch("/api/admin/lists/" + listId + "/initial-drops")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"initialDropCount": 200}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining").value(140))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.completionDate").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void technicianCanFinishAMixedListWithProblemDrops() throws Exception {
        User technician = saveTechnician("WL-T7", "WLNIP7", "secret12");
        String token = login(technician.getNip(), "secret12");

        MvcResult created = mockMvc.perform(post("/api/lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listType": "OBD_DPO",
                                  "initialDropCount": 50,
                                  "referenceDate": "2026-10-21"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listType").value("OBD_DPO"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn();
        long listId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"2026-10-21","completedDrops":47}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workList.totalCompleted").value(47))
                .andExpect(jsonPath("$.workList.remaining").value(3))
                .andExpect(jsonPath("$.workList.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/lists/" + listId + "/finish-with-problems")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"Access blocked"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.totalCompleted").value(47))
                .andExpect(jsonPath("$.problemDropCount").value(3))
                .andExpect(jsonPath("$.remaining").value(0))
                .andExpect(jsonPath("$.progressPercentage").value(94.0))
                .andExpect(jsonPath("$.problemNote").value("Access blocked"))
                .andExpect(jsonPath("$.completionDate").isNotEmpty());

        mockMvc.perform(post("/api/lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listType": "OBD",
                                  "initialDropCount": 20,
                                  "referenceDate": "2026-10-21"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void adminCannotCreateAWorkListThroughTechnicianEndpoint() throws Exception {
        String adminToken = login("ADMIN001", "admin123");

        mockMvc.perform(post("/api/lists")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listType": "OBD",
                                  "initialDropCount": 100,
                                  "referenceDate": "2026-09-17"
                                }
                                """))
                .andExpect(status().isForbidden());
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

    private void addProgress(Long listId, User technician, LocalDate date, int drops) {
        WorkList list = workListRepository.findById(listId).orElseThrow();
        DailyProgress progress = new DailyProgress();
        progress.setWorkList(list);
        progress.setTechnician(technician);
        progress.setWorkDate(date);
        progress.setCompletedDrops(drops);
        dailyProgressRepository.saveAndFlush(progress);
    }

    private User saveTechnician(String code, String nip, String rawPassword) {
        User user = new User();
        user.setTechnicianCode(code);
        user.setFirstName("Work");
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
