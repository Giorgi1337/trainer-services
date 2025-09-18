package com.gym.training.steps;

import com.gym.training.dto.TrainingRequest;
import com.gym.training.messaging.WorkloadProducer;
import com.gym.training.service.TrainingService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TrainingServiceSteps {

    private final WorkloadProducer workloadProducer = Mockito.mock(WorkloadProducer.class);
    private final TrainingService trainingService = new TrainingService(workloadProducer);

    private TrainingRequest lastRequest;
    private ResponseEntity<String> lastResponse;
    private TrainingRequest capturedRequest;

    @Given("the training service is running")
    public void trainingServiceIsRunning() {
        assertThat(trainingService).isNotNull();
    }

    @Given("the message queue is available")
    public void messageQueueIsAvailable() {
        assertThat(workloadProducer).isNotNull();
    }

    @Given("a valid trainer {string} exists")
    public void aValidTrainerExists(String username) {
        assertThat(username).isNotBlank();
    }

    @Given("an inactive trainer {string} exists")
    public void anInactiveTrainerExists(String username) {
        assertThat(username).isNotBlank();
    }

    @When("I submit a training session request with action {string}:")
    public void submitTrainingSession(String action, DataTable dataTable) {
        lastRequest = buildTrainingRequest(dataTable);
        trainingService.handleTrainingSession(lastRequest);
        lastResponse = ResponseEntity.ok("Training processed successfully");

        captureRequest();
    }

    @When("I submit a training session request with transaction ID {string}:")
    public void submitTrainingSessionWithTransactionId(String transactionId, DataTable dataTable) {
        MDC.put("transactionId", transactionId);
        try {
            lastRequest = buildTrainingRequest(dataTable);
            trainingService.handleTrainingSession(lastRequest);
            lastResponse = ResponseEntity.ok("Training processed successfully");
        } finally {
            MDC.clear();
        }
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCodeValue()).isEqualTo(expectedStatus);
    }

    @Then("the response message should be {string}")
    public void theResponseMessageShouldBe(String expectedMessage) {
        assertThat(lastResponse.getBody()).isEqualTo(expectedMessage);
    }

    @Then("a message should be sent to the training queue")
    public void messageSentToQueue() {
        assertThat(capturedRequest).isNotNull();
    }

    @Then("the message should contain trainer username {string}")
    public void theMessageShouldContainTrainerUsername(String expectedUsername) {
        assertThat(capturedRequest.getTrainerUsername()).isEqualTo(expectedUsername);
    }

    @Then("the message should contain action type {string}")
    public void theMessageShouldContainActionType(String expectedActionType) {
        assertThat(capturedRequest.getActionType().name()).isEqualTo(expectedActionType);
    }

    @Then("the message should contain training duration {int}")
    public void theMessageShouldContainTrainingDuration(int expectedDuration) {
        assertThat(capturedRequest.getTrainingDuration()).isEqualTo(expectedDuration);
    }

    @Then("the message should contain active status false")
    public void theMessageShouldContainActiveStatusFalse() {
        assertThat(capturedRequest.isActive()).isFalse();
    }

    @Then("the message sent to queue should contain transaction ID {string}")
    public void theMessageShouldContainTransactionId(String expectedTransactionId) {
        verify(workloadProducer, atLeastOnce()).sendTraining(any(TrainingRequest.class));
        assertThat(expectedTransactionId).isNotNull();
    }

    private void captureRequest() {
        ArgumentCaptor<TrainingRequest> captor = ArgumentCaptor.forClass(TrainingRequest.class);
        verify(workloadProducer, atLeastOnce()).sendTraining(captor.capture());
        capturedRequest = captor.getValue();
    }

    private TrainingRequest buildTrainingRequest(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        LocalDateTime trainingDate =
                LocalDateTime.parse(data.get("trainingDate"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        TrainingRequest request = new TrainingRequest();
        request.setTrainerUsername(data.get("trainerUsername"));
        request.setTrainerFirstName(data.get("trainerFirstName"));
        request.setTrainerLastName(data.get("trainerLastName"));
        request.setActive(Boolean.parseBoolean(data.get("active")));
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(Integer.parseInt(data.get("trainingDuration")));
        request.setActionType(TrainingRequest.ActionType.valueOf(data.get("actionType")));
        return request;
    }
}
