package com.gym.training.service;

import com.gym.training.client.WorkloadClient;
import com.gym.training.dto.TrainingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final WorkloadClient workloadClient;

    public void handleTrainingSession(TrainingRequest request) {
        workloadClient.updateWorkload(request);
    }
}
