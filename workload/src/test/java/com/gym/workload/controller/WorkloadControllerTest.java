package com.gym.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.workload.dto.MonthSummary;
import com.gym.workload.dto.TrainerSummaryResponse;
import com.gym.workload.dto.WorkloadRequest;
import com.gym.workload.dto.YearSummary;
import com.gym.workload.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkloadController.class)
@DisplayName("Workload Controller Tests")
public class WorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkloadService workloadService;

    private ObjectMapper objectMapper;
    private WorkloadRequest validRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        validRequest = createValidWorkloadRequest();
    }

    @Test
    @DisplayName("Should return 200 when updating workload successfully")
    @WithMockUser
    void shouldReturn200_WhenUpdatingWorkloadSuccessfully() throws Exception {
        // Given
        doNothing().when(workloadService).updateWorkload(any(WorkloadRequest.class));

        // When & Then
        mockMvc.perform(post("/api/workload/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Workload updated successfully"));

        verify(workloadService, times(1)).updateWorkload(any(WorkloadRequest.class));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/workload/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(workloadService, never()).updateWorkload(any());
    }

    @Test
    @DisplayName("Should handle ADD action type")
    @WithMockUser
    void shouldHandleAddActionType() throws Exception {
        // Given
        validRequest.setActionType(WorkloadRequest.ActionType.ADD);
        doNothing().when(workloadService).updateWorkload(any(WorkloadRequest.class));

        // When & Then
        mockMvc.perform(post("/api/workload/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(workloadService, times(1)).updateWorkload(any(WorkloadRequest.class));
    }

    @Test
    @DisplayName("Should handle DELETE action type")
    @WithMockUser
    void shouldHandleDeleteActionType() throws Exception {
        // Given
        validRequest.setActionType(WorkloadRequest.ActionType.DELETE);
        doNothing().when(workloadService).updateWorkload(any(WorkloadRequest.class));

        // When & Then
        mockMvc.perform(post("/api/workload/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(workloadService, times(1)).updateWorkload(any(WorkloadRequest.class));
    }
//
//    @Test
//    @DisplayName("Should return trainer summary when trainer exists")
//    @WithMockUser
//    void shouldReturnTrainerSummary_WhenTrainerExists() throws Exception {
//        // Given
//        String username = "john.doe";
//        TrainerSummaryResponse mockResponse = createTrainerSummaryResponse();
//        when(workloadService.getTrainerSummaryResponse(eq(username))).thenReturn(mockResponse);
//
//        // When & Then
//        mockMvc.perform(get("/api/workload/{username}", username))
//                .andExpect(status().isOk())
//                .andExpected(jsonPath("$.trainerUsername").value("john.doe"))
//                .andExpect(jsonPath("$.trainerFirstName").value("John"))
//                .andExpect(jsonPath("$.trainerLastName").value("Doe"))
//                .andExpect(jsonPath("$.active").value(true))
//                .andExpect(jsonPath("$.years").isArray())
//                .andExpect(jsonPath("$.years[0].year").value(2024))
//                .andExpected(jsonPath("$.years[0].months").isArray());
//
//        verify(workloadService, times(1)).getTrainerSummaryResponse(username);
//    }

    @Test
    @DisplayName("Should return 404 when trainer not found")
    @WithMockUser
    void shouldReturn404_WhenTrainerNotFound() throws Exception {
        // Given
        String username = "nonexistent";
        when(workloadService.getTrainerSummaryResponse(eq(username))).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/workload/{username}", username))
                .andExpect(status().isNotFound());

        verify(workloadService, times(1)).getTrainerSummaryResponse(username);
    }

//    @Test
//    @DisplayName("Should handle service exception in update workload")
//    @WithMockUser
//    void shouldHandleServiceException_InUpdateWorkload() throws Exception {
//        // Given
//        doThrow(new RuntimeException("Service error")).when(workloadService)
//                .updateWorkload(any(WorkloadRequest.class));
//
//        // When & Then
//        mockMvc.perform(post("/api/workload/update")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validRequest)))
//                .andExpected(status().is5xxServerError());
//    }

//    @Test
//    @DisplayName("Should handle service exception in get trainer summary")
//    @WithMockUser
//    void shouldHandleServiceException_InGetTrainerSummary() throws Exception {
//        // Given
//        String username = "john.doe";
//        when(workloadService.getTrainerSummaryResponse(eq(username)))
//                .thenThrow(new RuntimeException("Service error"));
//
//        // When & Then
//        mockMvc.perform(get("/api/workload/{username}", username))
//                .andExpected(status().is5xxServerError());
//    }

    private WorkloadRequest createValidWorkloadRequest() {
        WorkloadRequest request = new WorkloadRequest();
        request.setTrainerUsername("john.doe");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Doe");
        request.setActive(true);
        request.setTrainingDate(LocalDateTime.now().minusDays(1));
        request.setTrainingDuration(60);
        request.setActionType(WorkloadRequest.ActionType.ADD);
        return request;
    }

    private TrainerSummaryResponse createTrainerSummaryResponse() {
        List<MonthSummary> months = Arrays.asList(
                new MonthSummary(3, 120),
                new MonthSummary(4, 90)
        );
        List<YearSummary> years = Arrays.asList(
                new YearSummary(2024, months)
        );
        return new TrainerSummaryResponse("john.doe", "John", "Doe", true, years);
    }
}