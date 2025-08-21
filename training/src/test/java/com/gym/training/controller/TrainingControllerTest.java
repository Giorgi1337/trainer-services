package com.gym.training.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.training.dto.TrainingRequest;
import com.gym.training.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingController.class)
@DisplayName("Training Controller Tests")
public class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingService trainingService;

    private ObjectMapper objectMapper;
    private TrainingRequest validRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        validRequest = createValidTrainingRequest();
    }

    @Test
    @DisplayName("Should return 200 when processing valid training request")
    void shouldReturn200_WhenProcessingValidTrainingRequest() throws Exception {
        // Given
        doNothing().when(trainingService).handleTrainingSession(any(TrainingRequest.class));

        // When & Then
        mockMvc.perform(post("/api/training/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Training processed successfully"));

        verify(trainingService, times(1)).handleTrainingSession(any(TrainingRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when request body is missing")
    void shouldReturn400_WhenRequestBodyIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/training/session")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(trainingService, never()).handleTrainingSession(any());
    }

    @Test
    @DisplayName("Should handle ADD action type")
    void shouldHandleAddActionType() throws Exception {
        // Given
        validRequest.setActionType(TrainingRequest.ActionType.ADD);
        doNothing().when(trainingService).handleTrainingSession(any(TrainingRequest.class));

        // When & Then
        mockMvc.perform(post("/api/training/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(trainingService, times(1)).handleTrainingSession(any(TrainingRequest.class));
    }

    @Test
    @DisplayName("Should handle DELETE action type")
    void shouldHandleDeleteActionType() throws Exception {
        // Given
        validRequest.setActionType(TrainingRequest.ActionType.DELETE);
        doNothing().when(trainingService).handleTrainingSession(any(TrainingRequest.class));

        // When & Then
        mockMvc.perform(post("/api/training/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(trainingService, times(1)).handleTrainingSession(any(TrainingRequest.class));
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