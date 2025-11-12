/*
 * @ {#} TestServiceImpl.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import fit.instrument_service.dtos.request.CreateTestRequest;
import fit.instrument_service.dtos.response.TestResponse;
import fit.instrument_service.entities.Test;
import fit.instrument_service.enums.TestStatus;
import fit.instrument_service.exceptions.NotFoundException;
import fit.instrument_service.mappers.TestMapper;
import fit.instrument_service.repositories.TestRepository;
import fit.instrument_service.services.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    private final TestRepository testRepository;

    private final TestMapper testMapper;

    @Override
    public TestResponse createTest(CreateTestRequest request) {
        Test test = Test.builder()
                .name(request.getName())
                .description(request.getDescription())
                .score(request.getScore())
                .status(TestStatus.valueOf(request.getStatus()))
                .build();

        Test savedTest = testRepository.save(test);
        return testMapper.toResponse(savedTest);
    }

    @Override
    public TestResponse getTestById(String testId) {
        Test test = testRepository.findByTestId(testId)
                .orElseThrow(() -> new NotFoundException("Test not found with id: " + testId));

        return testMapper.toResponse(test);
    }
}
