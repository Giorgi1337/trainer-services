package com.gym.workload.service;

import com.gym.workload.dto.MonthSummary;
import com.gym.workload.dto.TrainerSummaryResponse;
import com.gym.workload.dto.WorkloadRequest;
import com.gym.workload.dto.YearSummary;
import com.gym.workload.model.TrainerSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Workload Service Tests")
public class WorkloadServiceTest {

    @InjectMocks
    private WorkloadService workloadService;

    private WorkloadRequest addRequest;
    private WorkloadRequest deleteRequest;

    @BeforeEach
    void setUp() {
        addRequest = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 60);
        deleteRequest = createWorkloadRequest(WorkloadRequest.ActionType.DELETE, 30);
    }

    @Test
    @DisplayName("Should create new trainer summary when trainer doesn't exist")
    void shouldCreateNewTrainerSummary_WhenTrainerDoesntExist() {
        // When
        workloadService.updateWorkload(addRequest);

        // Then
        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");
        assertNotNull(summary);
        assertEquals("john.doe", summary.getTrainerUsername());
        assertEquals("John", summary.getTrainerFirstName());
        assertEquals("Doe", summary.getTrainerLastName());
        assertTrue(summary.isActive());
    }

    @Test
    @DisplayName("Should add training duration for ADD action")
    void shouldAddTrainingDuration_ForAddAction() {
        // Given
        LocalDateTime trainingDate = LocalDateTime.of(2024, 3, 15, 10, 0);
        addRequest.setTrainingDate(trainingDate);
        addRequest.setTrainingDuration(90);

        // When
        workloadService.updateWorkload(addRequest);

        // Then
        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");
        Integer totalDuration = summary.getYearlySummary().get(2024).get(3);
        assertEquals(90, totalDuration);
    }

    @Test
    @DisplayName("Should subtract training duration for DELETE action")
    void shouldSubtractTrainingDuration_ForDeleteAction() {
        // Given - First add some training
        addRequest.setTrainingDuration(120);
        workloadService.updateWorkload(addRequest);

        // When - Delete part of it
        deleteRequest.setTrainingDate(addRequest.getTrainingDate());
        deleteRequest.setTrainingDuration(30);
        workloadService.updateWorkload(deleteRequest);

        // Then
        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");
        int year = addRequest.getTrainingDate().getYear();
        int month = addRequest.getTrainingDate().getMonthValue();
        Integer totalDuration = summary.getYearlySummary().get(year).get(month);
        assertEquals(90, totalDuration);
    }

    @Test
    @DisplayName("Should not go below zero when deleting more than available")
    void shouldNotGoBelowZero_WhenDeletingMoreThanAvailable() {
        // Given - Add 60 minutes
        workloadService.updateWorkload(addRequest);

        // When - Try to delete 120 minutes
        deleteRequest.setTrainingDate(addRequest.getTrainingDate());
        deleteRequest.setTrainingDuration(120);
        workloadService.updateWorkload(deleteRequest);

        // Then
        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");
        int year = addRequest.getTrainingDate().getYear();
        int month = addRequest.getTrainingDate().getMonthValue();
        Integer totalDuration = summary.getYearlySummary().get(year).get(month);
        assertEquals(0, totalDuration);
    }

    @Test
    @DisplayName("Should handle multiple months in same year")
    void shouldHandleMultipleMonthsInSameYear() {
        // Given
        WorkloadRequest marchRequest = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 60);
        marchRequest.setTrainingDate(LocalDateTime.of(2024, 3, 15, 10, 0));

        WorkloadRequest aprilRequest = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 90);
        aprilRequest.setTrainingDate(LocalDateTime.of(2024, 4, 15, 10, 0));

        // When
        workloadService.updateWorkload(marchRequest);
        workloadService.updateWorkload(aprilRequest);

        // Then
        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");
        assertEquals(60, (int) summary.getYearlySummary().get(2024).get(3));
        assertEquals(90, (int) summary.getYearlySummary().get(2024).get(4));
    }

    @Test
    @DisplayName("Should handle multiple years")
    void shouldHandleMultipleYears() {
        // Given
        WorkloadRequest year2024Request = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 60);
        year2024Request.setTrainingDate(LocalDateTime.of(2024, 3, 15, 10, 0));

        WorkloadRequest year2025Request = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 90);
        year2025Request.setTrainingDate(LocalDateTime.of(2025, 3, 15, 10, 0));

        // When
        workloadService.updateWorkload(year2024Request);
        workloadService.updateWorkload(year2025Request);

        // Then
        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");
        assertEquals(60, (int) summary.getYearlySummary().get(2024).get(3));
        assertEquals(90, (int) summary.getYearlySummary().get(2025).get(3));
    }

    @Test
    @DisplayName("Should accumulate training duration in same month")
    void shouldAccumulateTrainingDuration_InSameMonth() {
        // Given - Multiple trainings in same month
        WorkloadRequest firstTraining = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 60);
        WorkloadRequest secondTraining = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 45);
        WorkloadRequest thirdTraining = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 30);

        LocalDateTime sameMonth = LocalDateTime.of(2024, 3, 15, 10, 0);
        firstTraining.setTrainingDate(sameMonth);
        secondTraining.setTrainingDate(sameMonth.plusDays(5));
        thirdTraining.setTrainingDate(sameMonth.plusDays(10));

        // When
        workloadService.updateWorkload(firstTraining);
        workloadService.updateWorkload(secondTraining);
        workloadService.updateWorkload(thirdTraining);

        // Then
        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");
        Integer totalDuration = summary.getYearlySummary().get(2024).get(3);
        assertEquals(135, totalDuration);
    }

    @Test
    @DisplayName("Should return null for non-existent trainer")
    void shouldReturnNull_ForNonExistentTrainer() {
        // When
        TrainerSummary result = workloadService.getTrainerSummary("nonexistent");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null trainer summary response for non-existent trainer")
    void shouldReturnNullTrainerSummaryResponse_ForNonExistentTrainer() {
        // When
        TrainerSummaryResponse result = workloadService.getTrainerSummaryResponse("nonexistent");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert trainer summary to response correctly")
    void shouldConvertTrainerSummaryToResponse_Correctly() {
        // Given
        addRequest.setTrainingDate(LocalDateTime.of(2024, 3, 15, 10, 0));
        addRequest.setTrainingDuration(60);
        workloadService.updateWorkload(addRequest);

        // Add another month
        WorkloadRequest aprilRequest = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 90);
        aprilRequest.setTrainingDate(LocalDateTime.of(2024, 4, 15, 10, 0));
        workloadService.updateWorkload(aprilRequest);

        // When
        TrainerSummaryResponse response = workloadService.getTrainerSummaryResponse("john.doe");

        // Then
        assertNotNull(response);
        assertEquals("john.doe", response.getTrainerUsername());
        assertEquals("John", response.getTrainerFirstName());
        assertEquals("Doe", response.getTrainerLastName());
        assertTrue(response.isActive());

        List<YearSummary> years = response.getYears();
        assertEquals(1, years.size());

        YearSummary yearSummary = years.get(0);
        assertEquals(2024, yearSummary.getYear());

        List<MonthSummary> months = yearSummary.getMonths();
        assertEquals(2, months.size());

        // Check month summaries (order may vary)
        boolean foundMarch = false, foundApril = false;
        for (MonthSummary month : months) {
            if (month.getMonth() == 3) {
                assertEquals(60, month.getTotalDuration());
                foundMarch = true;
            } else if (month.getMonth() == 4) {
                assertEquals(90, month.getTotalDuration());
                foundApril = true;
            }
        }
        assertTrue(foundMarch && foundApril);
    }

    @Test
    @DisplayName("Should update existing trainer information")
    void shouldUpdateExistingTrainerInformation() {
        // Given - Create trainer first
        workloadService.updateWorkload(addRequest);

        // When - Update with new information
        WorkloadRequest updateRequest = createWorkloadRequest(WorkloadRequest.ActionType.ADD, 30);
        updateRequest.setTrainerFirstName("Johnny");
        updateRequest.setTrainerLastName("Doe-Smith");
        updateRequest.setActive(false);
        workloadService.updateWorkload(updateRequest);

        // Then - Trainer info should remain as original (first entry wins)
        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");
        assertEquals("John", summary.getTrainerFirstName());
        assertEquals("Doe", summary.getTrainerLastName());
        assertTrue(summary.isActive());
    }

    private WorkloadRequest createWorkloadRequest(WorkloadRequest.ActionType actionType, int duration) {
        WorkloadRequest request = new WorkloadRequest();
        request.setTrainerUsername("john.doe");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Doe");
        request.setActive(true);
        request.setTrainingDate(LocalDateTime.now().minusDays(1));
        request.setTrainingDuration(duration);
        request.setActionType(actionType);
        return request;
    }
}