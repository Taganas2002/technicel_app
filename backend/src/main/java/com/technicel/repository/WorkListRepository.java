package com.technicel.repository;

import com.technicel.entity.User;
import com.technicel.entity.WorkList;
import com.technicel.entity.WorkListStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkListRepository extends JpaRepository<WorkList, Long> {

    Optional<WorkList> findByTechnicianAndStatus(User technician, WorkListStatus status);

    Optional<WorkList> findByTechnicianIdAndStatus(Long technicianId, WorkListStatus status);

    boolean existsByTechnicianAndStatus(User technician, WorkListStatus status);

    boolean existsByTechnicianIdAndStatus(Long technicianId, WorkListStatus status);

    List<WorkList> findByTechnicianOrderByCreatedAtDesc(User technician);

    List<WorkList> findByTechnicianIdOrderByCreatedAtDesc(Long technicianId);

    List<WorkList> findByTechnicianIdAndStatusOrderByCompletionDateDesc(Long technicianId, WorkListStatus status);
}
