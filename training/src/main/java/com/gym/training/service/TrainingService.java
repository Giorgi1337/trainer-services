package com.gym.training.service;

import com.gym.training.dto.TrainingRequest;
import com.gym.training.messaging.WorkloadProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);
    private final WorkloadProducer workloadProducer;

    public void handleTrainingSession(TrainingRequest request) {
        log.info("Sending training session to workload-service asynchronously");
        workloadProducer.sendTraining(request);
    }
}
