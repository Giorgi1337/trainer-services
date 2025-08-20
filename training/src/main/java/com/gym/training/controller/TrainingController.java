package com.gym.training.controller;

import com.gym.training.dto.TrainingRequest;
import com.gym.training.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping("/session")
    public ResponseEntity<String> addOrDeleteTraining(@RequestBody TrainingRequest request) {
        trainingService.handleTrainingSession(request);
        return ResponseEntity.ok("Training processed successfully");
    }
}
