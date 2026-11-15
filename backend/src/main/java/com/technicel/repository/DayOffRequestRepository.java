package com.technicel.repository;

import com.technicel.entity.DayOffRequest;
import com.technicel.entity.DayOffStatus;
import com.technicel.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayOffRequestRepository extends JpaRepository<DayOffRequest, Long> {

    List<DayOffRequest> findByTechnicianOrderByCreatedAtDesc(User technician);

    List<DayOffRequest> findByTechnicianIdOrderByCreatedAtDesc(Long technicianId);

    Page<DayOffRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<DayOffRequest> findByStatusOrderByCreatedAtDesc(DayOffStatus status, Pageable pageable);
}
