package com.gym.workload.messaging;

import com.gym.workload.dto.WorkloadRequest;
import com.gym.workload.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class WorkloadConsumer {

    private static final Logger log = LoggerFactory.getLogger(WorkloadConsumer.class);
    private final WorkloadService workloadService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${queues.training}")
    public void receiveTraining(Message message) {
        String transactionId = null;
        try {
            // Extract transaction ID from message headers
            transactionId = message.getStringProperty("transactionId");
            if (transactionId != null) {
                MDC.put("transactionId", transactionId);
            }

            String messageBody = ((TextMessage) message).getText();
            JsonNode jsonNode = objectMapper.readTree(messageBody);

            // Map to WorkloadRequest
            WorkloadRequest workloadRequest = new WorkloadRequest();
            workloadRequest.setTrainerUsername(jsonNode.get("trainerUsername").asText());
            workloadRequest.setTrainerFirstName(jsonNode.get("trainerFirstName").asText());
            workloadRequest.setTrainerLastName(jsonNode.get("trainerLastName").asText());
            workloadRequest.setActive(jsonNode.get("active").asBoolean());
            workloadRequest.setTrainingDate(objectMapper.convertValue(jsonNode.get("trainingDate"), java.time.LocalDateTime.class));
            workloadRequest.setTrainingDuration(jsonNode.get("trainingDuration").asInt());
            workloadRequest.setActionType(WorkloadRequest.ActionType.valueOf(jsonNode.get("actionType").asText()));

            log.info("Processing training session for trainer: {} [txId={}]",
                    workloadRequest.getTrainerUsername(), transactionId);
            workloadService.updateWorkload(workloadRequest);

        } catch (JMSException e) {
            log.error("Failed to extract transaction ID from message [txId={}]", transactionId, e);
        } catch (Exception e) {
            log.error("Failed to process training message [txId={}]", transactionId, e);
        } finally {
            MDC.clear();
        }
    }
}