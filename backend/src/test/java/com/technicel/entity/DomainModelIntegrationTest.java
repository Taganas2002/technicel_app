package com.technicel.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.technicel.repository.DailyProgressRepository;
import com.technicel.repository.DayOffRequestRepository;
import com.technicel.repository.UserRepository;
import com.technicel.repository.WorkListRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class DomainModelIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkListRepository workListRepository;

    @Autowired
    private DailyProgressRepository dailyProgressRepository;

    @Autowired
    private DayOffRequestRepository dayOffRequestRepository;

    @Test
    void persistsUserWorkListProgressAndDayOff() {
        User technician = new User();
        technician.setTechnicianCode("TECH-001");
        technician.setFirstName("John");
        technician.setLastName("Smith");
        technician.setNip("NIP001");
        technician.setPassword("hashed-password");
        technician.setRole(Role.TECHNICIAN);
        technician.setActive(true);
        userRepository.saveAndFlush(technician);

        WorkList list = new WorkList();
        list.setTechnician(technician);
        list.setListType(ListType.OBD);
        list.setInitialDropCount(100);
        list.setReferenceDate(LocalDate.of(2026, 9, 17));
        list.setStatus(WorkListStatus.IN_PROGRESS);
        workListRepository.saveAndFlush(list);

        DailyProgress monday = new DailyProgress();
        monday.setWorkList(list);
        monday.setTechnician(technician);
        monday.setWorkDate(LocalDate.of(2026, 9, 17));
        monday.setCompletedDrops(20);
        dailyProgressRepository.saveAndFlush(monday);

        DailyProgress tuesday = new DailyProgress();
        tuesday.setWorkList(list);
        tuesday.setTechnician(technician);
        tuesday.setWorkDate(LocalDate.of(2026, 9, 18));
        tuesday.setCompletedDrops(25);
        dailyProgressRepository.saveAndFlush(tuesday);

        DayOffRequest dayOff = new DayOffRequest();
        dayOff.setTechnician(technician);
        dayOff.setStartDate(LocalDate.of(2026, 9, 20));
        dayOff.setEndDate(LocalDate.of(2026, 9, 21));
        dayOff.setNumberOfDays(2);
        dayOff.setReason("Family");
        dayOff.setStatus(DayOffStatus.PENDING);
        dayOffRequestRepository.saveAndFlush(dayOff);

        assertThat(userRepository.findByNip("NIP001")).isPresent();
        assertThat(workListRepository.existsByTechnicianAndStatus(technician, WorkListStatus.IN_PROGRESS)).isTrue();
        assertThat(dailyProgressRepository.findByWorkListOrderByWorkDateAsc(list)).hasSize(2);
        assertThat(dayOffRequestRepository.findByTechnicianOrderByCreatedAtDesc(technician)).hasSize(1);
        assertThat(technician.getCreatedAt()).isNotNull();
        assertThat(list.getId()).isNotNull();
    }

    @Test
    void rejectsDuplicateNip() {
        User first = technician("TECH-010", "NIP010");
        userRepository.saveAndFlush(first);

        User duplicate = technician("TECH-011", "NIP010");
        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateTechnicianCode() {
        userRepository.saveAndFlush(technician("TECH-020", "NIP020"));

        User duplicate = technician("TECH-020", "NIP021");
        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsOnlyOneInProgressListPerTechnician() {
        User technician = userRepository.saveAndFlush(technician("TECH-030", "NIP030"));

        WorkList first = inProgressList(technician);
        workListRepository.saveAndFlush(first);

        WorkList second = inProgressList(technician);
        assertThatThrownBy(() -> workListRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsTwoProgressRowsForTheSameDay() {
        User technician = userRepository.saveAndFlush(technician("TECH-040", "NIP040"));
        WorkList list = workListRepository.saveAndFlush(inProgressList(technician));

        DailyProgress first = progress(list, technician, LocalDate.of(2026, 9, 17), 10);
        dailyProgressRepository.saveAndFlush(first);

        DailyProgress duplicateDay = progress(list, technician, LocalDate.of(2026, 9, 17), 15);
        assertThatThrownBy(() -> dailyProgressRepository.saveAndFlush(duplicateDay))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User technician(String code, String nip) {
        User user = new User();
        user.setTechnicianCode(code);
        user.setFirstName("Ada");
        user.setLastName("Technician");
        user.setNip(nip);
        user.setPassword("hashed-password");
        user.setRole(Role.TECHNICIAN);
        user.setActive(true);
        return user;
    }

    private WorkList inProgressList(User technician) {
        WorkList list = new WorkList();
        list.setTechnician(technician);
        list.setListType(ListType.DPO);
        list.setInitialDropCount(50);
        list.setReferenceDate(LocalDate.of(2026, 9, 17));
        list.setStatus(WorkListStatus.IN_PROGRESS);
        return list;
    }

    private DailyProgress progress(WorkList list, User technician, LocalDate date, int drops) {
        DailyProgress progress = new DailyProgress();
        progress.setWorkList(list);
        progress.setTechnician(technician);
        progress.setWorkDate(date);
        progress.setCompletedDrops(drops);
        return progress;
    }
}
