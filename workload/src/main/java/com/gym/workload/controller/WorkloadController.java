package com.gym.workload.controller;

import com.gym.workload.dto.TrainerSummaryResponse;
import com.gym.workload.dto.WorkloadRequest;
import com.gym.workload.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {
    private final WorkloadService workloadService;

    @PostMapping("/update")
    public ResponseEntity<String> updateWorkload(@RequestBody WorkloadRequest request) {
        workloadService.updateWorkload(request);
        return ResponseEntity.ok("Workload updated successfully");
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummaryResponse> getTrainerSummary(@PathVariable("username") String username) {
        TrainerSummaryResponse summaryResponse = workloadService.getTrainerSummaryResponse(username);
        if (summaryResponse == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(summaryResponse);
    }
}
