package com.technicel.service;

import com.technicel.dto.AdminNotificationListResponse;
import com.technicel.dto.AdminNotificationResponse;
import com.technicel.entity.AdminNotification;
import com.technicel.entity.ListType;
import com.technicel.entity.NotificationType;
import com.technicel.entity.User;
import com.technicel.entity.WorkList;
import com.technicel.repository.AdminNotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;

    public AdminNotificationService(AdminNotificationRepository adminNotificationRepository) {
        this.adminNotificationRepository = adminNotificationRepository;
    }

    @Transactional
    public void notifyListCreated(User technician, WorkList list) {
        AdminNotification notification = new AdminNotification();
        notification.setType(NotificationType.LIST_CREATED);
        notification.setTitle("New list created");
        notification.setMessage(
                technician.getFirstName()
                        + " "
                        + technician.getLastName()
                        + " created a new "
                        + listTypeLabel(list.getListType())
                        + " list ("
                        + list.getInitialDropCount()
                        + " drops)."
        );
        notification.setTechnician(technician);
        notification.setWorkList(list);
        adminNotificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public AdminNotificationListResponse listRecent() {
        List<AdminNotificationResponse> notifications = adminNotificationRepository
                .findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
        return new AdminNotificationListResponse(adminNotificationRepository.countByReadAtIsNull(), notifications);
    }

    @Transactional
    public AdminNotificationResponse markRead(Long id) {
        AdminNotification notification = adminNotificationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification was not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notification = adminNotificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public AdminNotificationListResponse markAllRead() {
        adminNotificationRepository.markAllRead(LocalDateTime.now());
        return listRecent();
    }

    private String listTypeLabel(ListType listType) {
        return listType == ListType.OBD_DPO ? "OBD + DPO" : listType.name();
    }

    private AdminNotificationResponse toResponse(AdminNotification notification) {
        User technician = notification.getTechnician();
        WorkList list = notification.getWorkList();
        Long technicianId = technician == null ? null : technician.getId();
        String technicianCode = technician == null ? null : technician.getTechnicianCode();
        String technicianName = technician == null ? null : technician.getFirstName() + " " + technician.getLastName();
        Long workListId = list == null ? null : list.getId();
        ListType listType = list == null ? null : list.getListType();
        return new AdminNotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                technicianId,
                technicianCode,
                technicianName,
                workListId,
                listType,
                notification.getReadAt() != null,
                notification.getCreatedAt()
        );
    }
}
