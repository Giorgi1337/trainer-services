package com.gym.workload.steps;

import com.gym.workload.WorkloadApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(
        classes = WorkloadApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CucumberSpringConfiguration {
}