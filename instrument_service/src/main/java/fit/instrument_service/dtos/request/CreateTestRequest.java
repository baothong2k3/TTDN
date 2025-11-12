/*
 * @ {#} CreateTestRequest.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTestRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @Min(0)
    @Max(10)
    private Double score;

    @Pattern(
            regexp = "^(PENDING|IN_PROGRESS|COMPLETED|FAILED)$",
            message = "Status must be one of: PENDING, IN_PROGRESS, COMPLETED, FAILED"
    )
    private String status;
}
