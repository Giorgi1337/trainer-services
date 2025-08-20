package com.gym.workload.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkloadRequest {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean active;
    private LocalDateTime trainingDate;
    private int trainingDuration;
    private ActionType actionType;

    public enum ActionType {
        ADD, DELETE
    }
}
