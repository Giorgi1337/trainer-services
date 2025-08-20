package com.gym.workload.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TrainerSummary {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean active;

    // Map<Year, Map<Month, TotalMinutes>>
    private Map<Integer, Map<Integer, Integer>> yearlySummary = new HashMap<>();
}
