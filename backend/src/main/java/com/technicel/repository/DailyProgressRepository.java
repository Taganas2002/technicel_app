package com.technicel.repository;

import com.technicel.entity.DailyProgress;
import com.technicel.entity.WorkList;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyProgressRepository extends JpaRepository<DailyProgress, Long> {

    List<DailyProgress> findByWorkListOrderByWorkDateAsc(WorkList workList);

    Optional<DailyProgress> findByWorkListAndWorkDate(WorkList workList, LocalDate workDate);

    List<DailyProgress> findByWorkListIdOrderByWorkDateAsc(Long workListId);

    @Query("select coalesce(sum(p.completedDrops), 0) from DailyProgress p where p.workList.id = :workListId")
    Long sumCompletedByWorkListId(@Param("workListId") Long workListId);
}
