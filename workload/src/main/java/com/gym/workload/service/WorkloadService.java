package com.gym.workload.service;

import com.gym.workload.dto.MonthSummary;
import com.gym.workload.dto.TrainerSummaryResponse;
import com.gym.workload.dto.WorkloadRequest;
import com.gym.workload.dto.YearSummary;
import com.gym.workload.model.TrainerWorkloadDocument;
import com.gym.workload.model.TrainerWorkloadDocument.YearSummaryDocument;
import com.gym.workload.model.TrainerWorkloadDocument.MonthSummaryDocument;
import com.gym.workload.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadService.class);
    private final TrainerWorkloadRepository trainerWorkloadRepository;

    public void updateWorkload(WorkloadRequest request) {
        log.debug("Processing workload update for trainer: {}", request.getTrainerUsername());

        // Extract year and month from training date
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        // Find or create trainer document
        TrainerWorkloadDocument trainerDoc = trainerWorkloadRepository
                .findByTrainerUsername(request.getTrainerUsername())
                .orElse(createNewTrainerDocument(request));

        // Update trainer information (in case it changed)
        updateTrainerInfo(trainerDoc, request);

        // Find or create year summary
        YearSummaryDocument yearSummary = findOrCreateYearSummary(trainerDoc, year);

        // Find or create month summary
        MonthSummaryDocument monthSummary = findOrCreateMonthSummary(yearSummary, month);

        // Update training duration based on action type
        updateTrainingDuration(monthSummary, request);

        // Save the updated document
        trainerWorkloadRepository.save(trainerDoc);

        log.info("Successfully updated workload for trainer: {} - Year: {}, Month: {}, Duration: {}",
                request.getTrainerUsername(), year, month, monthSummary.getTotalDuration());
    }

    public TrainerSummaryResponse getTrainerSummaryResponse(String username) {
        log.debug("Fetching trainer summary for username: {}", username);

        Optional<TrainerWorkloadDocument> trainerDoc = trainerWorkloadRepository.findByTrainerUsername(username);

        if (trainerDoc.isEmpty()) {
            log.warn("No trainer found with username: {}", username);
            return null;
        }

        return convertToResponse(trainerDoc.get());
    }

    private TrainerWorkloadDocument createNewTrainerDocument(WorkloadRequest request) {
        log.debug("Creating new trainer document for username: {}", request.getTrainerUsername());

        TrainerWorkloadDocument trainerDoc = new TrainerWorkloadDocument();
        trainerDoc.setTrainerUsername(request.getTrainerUsername());
        trainerDoc.setTrainerFirstName(request.getTrainerFirstName());
        trainerDoc.setTrainerLastName(request.getTrainerLastName());
        trainerDoc.setActive(request.isActive());

        return trainerDoc;
    }

    private void updateTrainerInfo(TrainerWorkloadDocument trainerDoc, WorkloadRequest request) {
        // Update trainer information in case it has changed
        trainerDoc.setTrainerFirstName(request.getTrainerFirstName());
        trainerDoc.setTrainerLastName(request.getTrainerLastName());
        trainerDoc.setActive(request.isActive());
    }

    private YearSummaryDocument findOrCreateYearSummary(TrainerWorkloadDocument trainerDoc, int year) {
        return trainerDoc.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummaryDocument newYear = new YearSummaryDocument();
                    newYear.setYear(year);
                    trainerDoc.getYears().add(newYear);
                    return newYear;
                });
    }

    private MonthSummaryDocument findOrCreateMonthSummary(YearSummaryDocument yearSummary, int month) {
        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummaryDocument newMonth = new MonthSummaryDocument();
                    newMonth.setMonth(month);
                    newMonth.setTotalDuration(0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });
    }

    private void updateTrainingDuration(MonthSummaryDocument monthSummary, WorkloadRequest request) {
        int currentDuration = monthSummary.getTotalDuration();
        int trainingDuration = request.getTrainingDuration();

        if (request.getActionType() == WorkloadRequest.ActionType.ADD) {
            monthSummary.setTotalDuration(currentDuration + trainingDuration);
            log.debug("Added {} minutes. New total: {}", trainingDuration, monthSummary.getTotalDuration());
        } else if (request.getActionType() == WorkloadRequest.ActionType.DELETE) {
            int newDuration = Math.max(0, currentDuration - trainingDuration);
            monthSummary.setTotalDuration(newDuration);
            log.debug("Subtracted {} minutes. New total: {}", trainingDuration, monthSummary.getTotalDuration());
        }
    }

    private TrainerSummaryResponse convertToResponse(TrainerWorkloadDocument trainerDoc) {
        List<YearSummary> years = trainerDoc.getYears().stream()
                .map(yearDoc -> {
                    List<MonthSummary> months = yearDoc.getMonths().stream()
                            .map(monthDoc -> new MonthSummary(monthDoc.getMonth(), monthDoc.getTotalDuration()))
                            .collect(Collectors.toList());
                    return new YearSummary(yearDoc.getYear(), months);
                })
                .collect(Collectors.toList());

        return new TrainerSummaryResponse(
                trainerDoc.getTrainerUsername(),
                trainerDoc.getTrainerFirstName(),
                trainerDoc.getTrainerLastName(),
                trainerDoc.isActive(),
                years
        );
    }
}