package com.gym.workload.steps;

import com.gym.workload.dto.MonthSummary;
import com.gym.workload.dto.TrainerSummaryResponse;
import com.gym.workload.dto.WorkloadRequest;
import com.gym.workload.dto.YearSummary;
import com.gym.workload.model.TrainerWorkloadDocument;
import com.gym.workload.repository.TrainerWorkloadRepository;
import com.gym.workload.service.WorkloadService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class WorkloadServiceSteps {

    private final TrainerWorkloadRepository trainerWorkloadRepository = Mockito.mock(TrainerWorkloadRepository.class);
    private final WorkloadService workloadService = new WorkloadService(trainerWorkloadRepository);

    private WorkloadRequest lastRequest;
    private TrainerSummaryResponse lastResponse;
    private Exception lastException;

    @Given("the workload service is running")
    public void workloadServiceIsRunning() {
        assertThat(workloadService).isNotNull();
    }

    @Given("the MongoDB database is available")
    public void mongoDbIsAvailable() {
        assertThat(trainerWorkloadRepository).isNotNull();
    }

    @Given("the database is clean")
    public void databaseIsClean() {
        reset(trainerWorkloadRepository);
    }

    @Given("a trainer {string} with existing workload data:")
    public void aTrainerWithExistingWorkloadData(String username, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        TrainerWorkloadDocument existingDoc = new TrainerWorkloadDocument();
        existingDoc.setTrainerUsername(username);
        existingDoc.setTrainerFirstName(data.get("firstName"));
        existingDoc.setTrainerLastName(data.get("lastName"));
        existingDoc.setActive(Boolean.parseBoolean(data.get("active")));

        // Add existing workload data
        TrainerWorkloadDocument.YearSummaryDocument yearDoc = new TrainerWorkloadDocument.YearSummaryDocument();
        yearDoc.setYear(Integer.parseInt(data.get("year")));

        TrainerWorkloadDocument.MonthSummaryDocument monthDoc = new TrainerWorkloadDocument.MonthSummaryDocument();
        monthDoc.setMonth(Integer.parseInt(data.get("month")));
        monthDoc.setTotalDuration(Integer.parseInt(data.get("existingDuration")));

        yearDoc.getMonths().add(monthDoc);
        existingDoc.getYears().add(yearDoc);

        when(trainerWorkloadRepository.findByTrainerUsername(username)).thenReturn(Optional.of(existingDoc));
    }

    @When("I process a workload update with transaction ID {string}:")
    public void processWorkloadUpdateWithTransactionId(String transactionId, DataTable dataTable) {
        MDC.put("transactionId", transactionId);
        try {
            lastRequest = buildWorkloadRequest(dataTable);
            workloadService.updateWorkload(lastRequest);
        } catch (Exception e) {
            lastException = e;
        } finally {
            MDC.clear();
        }
    }

    @When("I process a workload update:")
    public void processWorkloadUpdate(DataTable dataTable) {
        try {
            lastRequest = buildWorkloadRequest(dataTable);
            workloadService.updateWorkload(lastRequest);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I request trainer summary for username {string}")
    public void requestTrainerSummary(String username) {
        try {
            lastResponse = workloadService.getTrainerSummaryResponse(username);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("no exception should be thrown")
    public void noExceptionShouldBeThrown() {
        assertThat(lastException).isNull();
    }

    @Then("the trainer document should be created in database")
    public void trainerDocumentShouldBeCreatedInDatabase() {
        verify(trainerWorkloadRepository, atLeastOnce()).save(any(TrainerWorkloadDocument.class));
    }

    @Then("the trainer document should contain:")
    public void trainerDocumentShouldContain(DataTable dataTable) {
        Map<String, String> expected = dataTable.asMap(String.class, String.class);

        ArgumentCaptor<TrainerWorkloadDocument> captor =
                ArgumentCaptor.forClass(TrainerWorkloadDocument.class);

        verify(trainerWorkloadRepository, atLeastOnce()).save(captor.capture());

        TrainerWorkloadDocument savedDoc = captor.getValue();

        assertThat(savedDoc.getTrainerUsername()).isEqualTo(expected.get("username"));
        assertThat(savedDoc.getTrainerFirstName()).isEqualTo(expected.get("firstName"));
        assertThat(savedDoc.getTrainerLastName()).isEqualTo(expected.get("lastName"));
        assertThat(savedDoc.isActive()).isEqualTo(Boolean.parseBoolean(expected.get("active")));
    }


    @Then("the workload for year {int} month {int} should be {int} minutes")
    public void workloadForYearMonthShouldBe(int year, int month, int expectedDuration) {
        ArgumentCaptor<TrainerWorkloadDocument> captor =
                ArgumentCaptor.forClass(TrainerWorkloadDocument.class);

        verify(trainerWorkloadRepository, atLeastOnce()).save(captor.capture());

        TrainerWorkloadDocument savedDoc = captor.getValue();

        TrainerWorkloadDocument.YearSummaryDocument yearDoc = savedDoc.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElse(null);

        assertThat(yearDoc).isNotNull();

        TrainerWorkloadDocument.MonthSummaryDocument monthDoc = yearDoc.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElse(null);

        assertThat(monthDoc).isNotNull();
        assertThat(monthDoc.getTotalDuration()).isEqualTo(expectedDuration);
    }

    @Then("the trainer summary should be returned")
    public void trainerSummaryShouldBeReturned() {
        assertThat(lastResponse).isNotNull();
    }

    @Then("the trainer summary should be null")
    public void trainerSummaryShouldBeNull() {
        assertThat(lastResponse).isNull();
    }

    @Then("the trainer summary should contain:")
    public void trainerSummaryShouldContain(DataTable dataTable) {
        Map<String, String> expected = dataTable.asMap(String.class, String.class);

        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getTrainerUsername()).isEqualTo(expected.get("username"));
        assertThat(lastResponse.getTrainerFirstName()).isEqualTo(expected.get("firstName"));
        assertThat(lastResponse.getTrainerLastName()).isEqualTo(expected.get("lastName"));
        assertThat(lastResponse.isActive()).isEqualTo(Boolean.parseBoolean(expected.get("active")));
    }

    @Then("the trainer summary should contain year {int} with total months {int}")
    public void trainerSummaryShouldContainYearWithTotalMonths(int year, int expectedMonthCount) {
        assertThat(lastResponse).isNotNull();

        YearSummary yearSummary = lastResponse.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElse(null);

        assertThat(yearSummary).isNotNull();
        assertThat(yearSummary.getMonths()).hasSize(expectedMonthCount);
    }

    @Then("the year {int} should contain month {int} with duration {int}")
    public void yearShouldContainMonthWithDuration(int year, int month, int expectedDuration) {
        assertThat(lastResponse).isNotNull();

        YearSummary yearSummary = lastResponse.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElse(null);

        assertThat(yearSummary).isNotNull();

        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElse(null);

        assertThat(monthSummary).isNotNull();
        assertThat(monthSummary.getTotalDuration()).isEqualTo(expectedDuration);
    }

    @Then("the minimum duration should be enforced to {int}")
    public void minimumDurationShouldBeEnforcedTo(int expectedMinimum) {
        Optional<TrainerWorkloadDocument> docOpt = trainerWorkloadRepository
                .findByTrainerUsername(lastRequest.getTrainerUsername());

        assertThat(docOpt).isPresent();
        TrainerWorkloadDocument doc = docOpt.get();

        int year = lastRequest.getTrainingDate().getYear();
        int month = lastRequest.getTrainingDate().getMonthValue();

        TrainerWorkloadDocument.YearSummaryDocument yearDoc = doc.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElse(null);

        assertThat(yearDoc).isNotNull();

        TrainerWorkloadDocument.MonthSummaryDocument monthDoc = yearDoc.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElse(null);

        assertThat(monthDoc).isNotNull();
        assertThat(monthDoc.getTotalDuration()).isEqualTo(expectedMinimum);
    }

    private WorkloadRequest buildWorkloadRequest(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        LocalDateTime trainingDate =
                LocalDateTime.parse(data.get("trainingDate"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        WorkloadRequest request = new WorkloadRequest();
        request.setTrainerUsername(data.get("trainerUsername"));
        request.setTrainerFirstName(data.get("trainerFirstName"));
        request.setTrainerLastName(data.get("trainerLastName"));
        request.setActive(Boolean.parseBoolean(data.get("active")));
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(Integer.parseInt(data.get("trainingDuration")));
        request.setActionType(WorkloadRequest.ActionType.valueOf(data.get("actionType")));
        return request;
    }
}
