package com.gym.training.client;

import com.gym.training.dto.TrainingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "workload-service", path = "/api/workload")
public interface WorkloadClient {

    @PostMapping("/update")
    void updateWorkload(@RequestBody TrainingRequest request);
}
