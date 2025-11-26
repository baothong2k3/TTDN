/*
 * @ (#) EventLogServiceImpl.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.dtos.event.SystemEvent;
import fit.monitoring_service.entities.EventLog;
import fit.monitoring_service.repositories.EventLogRepository;
import fit.monitoring_service.services.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogServiceImpl implements EventLogService {

    private final EventLogRepository eventLogRepository;

    @Override
    public void saveEventLog(SystemEvent event) {
        try {
            log.info("Saving event log: [{}] {}", event.getEventCode(), event.getAction());

            EventLog eventLog = EventLog.builder()
                    .eventCode(event.getEventCode())
                    .action(event.getAction())
                    .message(event.getMessage())
                    .sourceService(event.getSourceService())
                    .operator(event.getOperator())
                    .details(event.getDetails())
                    .ipAddress(event.getIpAddress())
                    .userAgent(event.getUserAgent())
                    .createdAt(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                    .build();

            eventLogRepository.save(eventLog);
            log.info("Event log saved successfully with ID: {}", eventLog.getId());

        } catch (Exception e) {
            log.error("Failed to save event log: {}", e.getMessage(), e);
            // Tùy chọn: Có thể throw exception hoặc gửi alert nếu cần
        }
    }
}
