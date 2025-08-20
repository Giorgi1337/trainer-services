package com.gym.workload.service;

import com.gym.workload.dto.MonthSummary;
import com.gym.workload.dto.TrainerSummaryResponse;
import com.gym.workload.dto.WorkloadRequest;
import com.gym.workload.dto.YearSummary;
import com.gym.workload.model.TrainerSummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WorkloadService {

    private final Map<String, TrainerSummary> trainerWorkloads = new ConcurrentHashMap<>();

    public void updateWorkload(WorkloadRequest request) {
        TrainerSummary summary = trainerWorkloads.computeIfAbsent(request.getTrainerUsername(), key -> {
            TrainerSummary t = new TrainerSummary();
            t.setTrainerUsername(request.getTrainerUsername());
            t.setTrainerFirstName(request.getTrainerFirstName());
            t.setTrainerLastName(request.getTrainerLastName());
            t.setActive(request.isActive());
            return t;
        });

        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        summary.getYearlySummary().putIfAbsent(year, new ConcurrentHashMap<>());
        summary.getYearlySummary().get(year).putIfAbsent(month, 0);

        if (request.getActionType() == WorkloadRequest.ActionType.ADD) {
            summary.getYearlySummary().get(year).put(
                    month,
                    summary.getYearlySummary().get(year).get(month) + request.getTrainingDuration()
            );
        } else if (request.getActionType() == WorkloadRequest.ActionType.DELETE) {
            summary.getYearlySummary().get(year).put(
                    month,
                    Math.max(0, summary.getYearlySummary().get(year).get(month) - request.getTrainingDuration())
            );
        }
    }

    // Raw model getter
    public TrainerSummary getTrainerSummary(String username) {
        return trainerWorkloads.get(username);
    }

    public TrainerSummaryResponse getTrainerSummaryResponse(String username) {
        TrainerSummary trainer = trainerWorkloads.get(username);
        if (trainer == null) return null;

        List<YearSummary> years = trainer.getYearlySummary().entrySet().stream()
                .map(yearEntry -> {
                    List<MonthSummary> months = yearEntry.getValue().entrySet().stream()
                            .map(monthEntry -> new MonthSummary(monthEntry.getKey(), monthEntry.getValue()))
                            .collect(Collectors.toList());
                    return new YearSummary(yearEntry.getKey(), months);
                })
                .collect(Collectors.toList());

        return new TrainerSummaryResponse(
                trainer.getTrainerUsername(),
                trainer.getTrainerFirstName(),
                trainer.getTrainerLastName(),
                trainer.isActive(),
                years
        );
    }

}
