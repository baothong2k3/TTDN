/*
 * @ {#} TestController.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.controllers;

import fit.instrument_service.dtos.request.CreateTestRequest;
import fit.instrument_service.dtos.response.ApiResponse;
import fit.instrument_service.dtos.response.TestResponse;
import fit.instrument_service.entities.Test;
import fit.instrument_service.services.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/instruments/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @PostMapping
    public ResponseEntity<ApiResponse<TestResponse>> createTest(@Valid @RequestBody CreateTestRequest request) {
        TestResponse createdTest = testService.createTest(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdTest, "Test created successfully"));
    }

    @GetMapping("/{testId}")
    public ResponseEntity<ApiResponse<TestResponse>> getTestById(@PathVariable String testId) {
        TestResponse test = testService.getTestById(testId);
        return ResponseEntity.ok(ApiResponse.success(test, "Test retrieved successfully"));
    }
}
