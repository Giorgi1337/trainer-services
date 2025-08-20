package com.gym.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthSummary {
    private int month;
    private int totalDuration;
}
