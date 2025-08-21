package com.gym.training.service;

import com.gym.training.client.WorkloadClient;
import com.gym.training.dto.TrainingRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);
    private static final String WORKLOAD_SERVICE_CB = "workloadServiceCB";

    private final WorkloadClient workloadClient;

    @CircuitBreaker(name = WORKLOAD_SERVICE_CB, fallbackMethod = "workloadFallback")
    public void handleTrainingSession(TrainingRequest request) {
        workloadClient.updateWorkload(request);
    }

    // fallback method
    private void workloadFallback(TrainingRequest request, Throwable t) {
        log.error("Workload service unavailable. Trainer={} Reason={}",
                request.getTrainerUsername(), t.getMessage());
    }
}
