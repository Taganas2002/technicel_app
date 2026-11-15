package com.technicel.repository;

import com.technicel.entity.AdminNotification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    @EntityGraph(attributePaths = {"technician", "workList"})
    List<AdminNotification> findTop50ByOrderByCreatedAtDesc();

    long countByReadAtIsNull();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update AdminNotification n set n.readAt = :now where n.readAt is null")
    int markAllRead(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update AdminNotification n set n.workList = null where n.workList.id = :listId")
    int detachWorkList(@Param("listId") Long listId);
}
