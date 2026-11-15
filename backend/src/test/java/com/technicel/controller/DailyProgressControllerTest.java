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
class DailyProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void firstProgressUpdatesTotals() throws Exception {
        User technician = saveTechnician("DP-T1", "DPNIP1", "secret12");
        String token = login(technician.getNip(), "secret12");
        long listId = createList(token, 100);

        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"2026-09-17","completedDrops":25}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.progress.completedDrops").value(25))
                .andExpect(jsonPath("$.progress.workDate").value("2026-09-17"))
                .andExpect(jsonPath("$.workList.totalCompleted").value(25))
                .andExpect(jsonPath("$.workList.remaining").value(75))
                .andExpect(jsonPath("$.workList.progressPercentage").value(25.0))
                .andExpect(jsonPath("$.workList.status").value("IN_PROGRESS"));
    }

    @Test
    void multipleDaysAreStoredSeparately() throws Exception {
        User technician = saveTechnician("DP-T2", "DPNIP2", "secret12");
        String token = login(technician.getNip(), "secret12");
        long listId = createList(token, 100);

        addProgress(token, listId, "2026-09-17", 20);
        addProgress(token, listId, "2026-09-18", 25);

        mockMvc.perform(get("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].workDate").value("2026-09-17"))
                .andExpect(jsonPath("$[0].completedDrops").value(20))
                .andExpect(jsonPath("$[1].workDate").value("2026-09-18"))
                .andExpect(jsonPath("$[1].completedDrops").value(25));

        mockMvc.perform(get("/api/lists/" + listId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCompleted").value(45))
                .andExpect(jsonPath("$.remaining").value(55));
    }

    @Test
    void reachingTotalMarksListFinished() throws Exception {
        User technician = saveTechnician("DP-T3", "DPNIP3", "secret12");
        String token = login(technician.getNip(), "secret12");
        long listId = createList(token, 100);

        addProgress(token, listId, "2026-09-17", 40);
        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"2026-09-18","completedDrops":60}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workList.totalCompleted").value(100))
                .andExpect(jsonPath("$.workList.remaining").value(0))
                .andExpect(jsonPath("$.workList.progressPercentage").value(100.0))
                .andExpect(jsonPath("$.workList.status").value("FINISHED"))
                .andExpect(jsonPath("$.workList.completionDate").isNotEmpty());
    }

    @Test
    void progressAboveInitialAmountIsRejected() throws Exception {
        User technician = saveTechnician("DP-T4", "DPNIP4", "secret12");
        String token = login(technician.getNip(), "secret12");
        long listId = createList(token, 100);

        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"2026-09-17","completedDrops":101}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Completed drops cannot exceed the initial drop count"));
    }

    @Test
    void technicianCannotAddProgressToSomeoneElsesList() throws Exception {
        User first = saveTechnician("DP-T5", "DPNIP5", "secret12");
        User second = saveTechnician("DP-T6", "DPNIP6", "secret12");
        String firstToken = login(first.getNip(), "secret12");
        String secondToken = login(second.getNip(), "secret12");
        long listId = createList(firstToken, 100);

        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"2026-09-17","completedDrops":10}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotAddProgressToFinishedList() throws Exception {
        User technician = saveTechnician("DP-T7", "DPNIP7", "secret12");
        String token = login(technician.getNip(), "secret12");
        long listId = createList(token, 50);
        addProgress(token, listId, "2026-09-17", 50);

        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"2026-09-18","completedDrops":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Finished work lists cannot receive more progress"));
    }

    @Test
    void sameDayProgressIsUpdatedNotDuplicated() throws Exception {
        User technician = saveTechnician("DP-T8", "DPNIP8", "secret12");
        String token = login(technician.getNip(), "secret12");
        long listId = createList(token, 100);
        addProgress(token, listId, "2026-09-17", 10);
        addProgress(token, listId, "2026-09-17", 30);

        mockMvc.perform(get("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].completedDrops").value(30));
    }

    @Test
    void sameDayCorrectionCanReopenFinishedList() throws Exception {
        User technician = saveTechnician("DP-T9", "DPNIP9", "secret12");
        String token = login(technician.getNip(), "secret12");
        long listId = createList(token, 50);
        addProgress(token, listId, "2026-09-17", 50);

        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"2026-09-17","completedDrops":40}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.progress.completedDrops").value(40))
                .andExpect(jsonPath("$.workList.totalCompleted").value(40))
                .andExpect(jsonPath("$.workList.remaining").value(10))
                .andExpect(jsonPath("$.workList.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.workList.completionDate").value(org.hamcrest.Matchers.nullValue()));
    }

    private void addProgress(String token, long listId, String date, int drops) throws Exception {
        mockMvc.perform(post("/api/lists/" + listId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workDate":"%s","completedDrops":%s}
                                """.formatted(date, drops)))
                .andExpect(status().isCreated());
    }

    private long createList(String token, int initialDrops) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listType": "OBD",
                                  "initialDropCount": %s,
                                  "referenceDate": "2026-09-17"
                                }
                                """.formatted(initialDrops)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private User saveTechnician(String code, String nip, String rawPassword) {
        User user = new User();
        user.setTechnicianCode(code);
        user.setFirstName("Daily");
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
