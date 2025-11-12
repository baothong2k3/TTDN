/*
 * @ {#} Test.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.entities;

import fit.instrument_service.enums.TestStatus;
import fit.instrument_service.markers.HasBusinessId;
import fit.instrument_service.utils.IdGenerator;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/*
 * @description: Entity class representing a Test document in MongoDB
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tests")
public class Test extends BaseDocument implements HasBusinessId {
    @Field("test_id")
    @Indexed(unique = true)
    private String testId;

    private String name;

    private String description;

    private Double score;

    private TestStatus status;

    @Override
    public void assignBusinessId() {
        if (this.testId == null || this.testId.isEmpty()) {
            this.testId = IdGenerator.generate("TEST");
        }
    }
}
