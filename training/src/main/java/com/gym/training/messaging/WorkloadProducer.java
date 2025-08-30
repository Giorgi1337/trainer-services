package com.gym.training.messaging;

import com.gym.training.dto.TrainingRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkloadProducer {

    private final JmsTemplate jmsTemplate;

    @Value("${queues.training}")
    private String trainingQueue;

    public void sendTraining(TrainingRequest request) {
        jmsTemplate.convertAndSend(trainingQueue, request, message -> {
            String txId = MDC.get("transactionId");
            message.setStringProperty("transactionId", txId);
            return message;
        });
    }
}