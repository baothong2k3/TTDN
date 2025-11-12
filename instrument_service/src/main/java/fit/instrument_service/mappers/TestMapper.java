/*
 * @ {#} TestMapper.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.mappers;

import fit.instrument_service.dtos.response.TestResponse;
import fit.instrument_service.entities.Test;
import org.springframework.stereotype.Component;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Component
public class TestMapper {
    public TestResponse toResponse(Test test) {
        if (test == null) return null;

        return TestResponse.builder()
                .testId(test.getTestId())
                .name(test.getName())
                .description(test.getDescription())
                .score(test.getScore())
                .status(test.getStatus())
                .createdAt(test.getCreatedAt())
                .createdBy(test.getCreatedBy())
                .updatedAt(test.getUpdatedAt())
                .updatedBy(test.getUpdatedBy())
                .isDeleted(test.isDeleted())
                .deletedAt(test.getDeletedAt())
                .build();
    }
}
