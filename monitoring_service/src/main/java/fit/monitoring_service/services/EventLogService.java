/*
 * @ (#) EventLogService.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.dtos.event.SystemEvent;

public interface EventLogService {
    void saveEventLog(SystemEvent event);
}
