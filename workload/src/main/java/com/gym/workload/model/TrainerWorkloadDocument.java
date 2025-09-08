package com.gym.workload.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "trainer_summaries")
public class TrainerWorkloadDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String trainerUsername;

    private String trainerFirstName;
    private String trainerLastName;
    private boolean active;

    private List<YearSummaryDocument> years = new ArrayList<>();

    @Data
    public static class YearSummaryDocument {
        private int year;
        private List<MonthSummaryDocument> months = new ArrayList<>();
    }

    @Data
    public static class MonthSummaryDocument {
        private int month;
        private int totalDuration;
    }
}