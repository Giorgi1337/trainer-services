package com.gym.training.service;

import com.gym.training.client.WorkloadClient;
import com.gym.training.dto.TrainingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Training Service Tests")
public class TrainingServiceTest {

    @Mock
    private WorkloadClient workloadClient;

    @InjectMocks
    private TrainingService trainingService;

    private TrainingRequest trainingRequest;

    @BeforeEach
    void setUp() {
        MDC.put("transactionId", "test-tx-123");
        trainingRequest = createValidTrainingRequest();
    }

    @Test
    @DisplayName("Should successfully handle training session - ADD action")
    void shouldHandleTrainingSession_AddAction() {
        // Given
        trainingRequest.setActionType(TrainingRequest.ActionType.ADD);

        // When
        assertDoesNotThrow(() -> trainingService.handleTrainingSession(trainingRequest));

        // Then
        verify(workloadClient, times(1)).updateWorkload(trainingRequest);
    }

    @Test
    @DisplayName("Should successfully handle training session - DELETE action")
    void shouldHandleTrainingSession_DeleteAction() {
        // Given
        trainingRequest.setActionType(TrainingRequest.ActionType.DELETE);

        // When
        assertDoesNotThrow(() -> trainingService.handleTrainingSession(trainingRequest));

        // Then
        verify(workloadClient, times(1)).updateWorkload(trainingRequest);
    }

    @Test
    @DisplayName("Should handle multiple training sessions")
    void shouldHandleMultipleTrainingSessions() {
        // Given
        TrainingRequest request1 = createValidTrainingRequest();
        TrainingRequest request2 = createValidTrainingRequest();
        request2.setTrainerUsername("trainer2");

        // When
        assertDoesNotThrow(() -> {
            trainingService.handleTrainingSession(request1);
            trainingService.handleTrainingSession(request2);
        });

        // Then
        verify(workloadClient, times(2)).updateWorkload(any(TrainingRequest.class));
    }


    private TrainingRequest createValidTrainingRequest() {
        TrainingRequest request = new TrainingRequest();
        request.setTrainerUsername("john.doe");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Doe");
        request.setActive(true);
        request.setTrainingDate(LocalDateTime.now().minusDays(1));
        request.setTrainingDuration(60);
        request.setActionType(TrainingRequest.ActionType.ADD);
        return request;
    }
}