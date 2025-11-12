/*
 * @ {#} TestService.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services;

import fit.instrument_service.dtos.request.CreateTestRequest;
import fit.instrument_service.dtos.response.TestResponse;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
public interface TestService {
    TestResponse createTest(CreateTestRequest request);
    TestResponse getTestById(String testId);
}
