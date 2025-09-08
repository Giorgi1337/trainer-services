package com.gym.workload.repository;

import com.gym.workload.model.TrainerWorkloadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkloadDocument, String> {

    Optional<TrainerWorkloadDocument> findByTrainerUsername(String trainerUsername);

    boolean existsByTrainerUsername(String trainerUsername);

    void deleteByTrainerUsername(String trainerUsername);
}