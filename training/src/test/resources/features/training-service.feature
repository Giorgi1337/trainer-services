Feature: Training Service Component Tests
  As a training service
  I want to handle training session requests
  So that workload data can be processed asynchronously

  Background:
    Given the training service is running
    And the message queue is available

  @positive @add
  Scenario: Successfully process ADD training session
    Given a valid trainer "john.doe" exists
    When I submit a training session request with action "ADD":
      | trainerUsername  | john.doe           |
      | trainerFirstName | John               |
      | trainerLastName  | Doe                |
      | active           | true               |
      | trainingDate     | 2024-12-15T10:00:00|
      | trainingDuration | 60                 |
      | actionType       | ADD                |
    Then the response status should be 200
    And the response message should be "Training processed successfully"
    And a message should be sent to the training queue
    And the message should contain trainer username "john.doe"
    And the message should contain action type "ADD"
    And the message should contain training duration 60

  @positive @delete
  Scenario: Successfully process DELETE training session
    Given a valid trainer "jane.smith" exists
    When I submit a training session request with action "DELETE":
      | trainerUsername  | jane.smith         |
      | trainerFirstName | Jane               |
      | trainerLastName  | Smith              |
      | active           | true               |
      | trainingDate     | 2024-12-15T14:00:00|
      | trainingDuration | 45                 |
      | actionType       | DELETE             |
    Then the response status should be 200
    And the response message should be "Training processed successfully"
    And a message should be sent to the training queue
    And the message should contain action type "DELETE"
    And the message should contain training duration 45

  @negative @inactiveTrainer
  Scenario: Handle inactive trainer session
    Given an inactive trainer "inactive.trainer" exists
    When I submit a training session request with action "ADD":
      | trainerUsername  | inactive.trainer   |
      | trainerFirstName | Inactive           |
      | trainerLastName  | Trainer            |
      | active           | false              |
      | trainingDate     | 2024-12-15T10:00:00|
      | trainingDuration | 30                 |
      | actionType       | ADD                |
    Then the response status should be 200
    And the response message should be "Training processed successfully"
    And a message should be sent to the training queue
    And the message should contain active status false

  @positive @transaction
  Scenario: Verify transaction ID propagation
    Given a valid trainer "john.doe" exists
    When I submit a training session request with transaction ID "txn-12345":
      | trainerUsername  | john.doe           |
      | trainerFirstName | John               |
      | trainerLastName  | Doe                |
      | active           | true               |
      | trainingDate     | 2024-12-15T10:00:00|
      | trainingDuration | 60                 |
      | actionType       | ADD                |
    Then the response status should be 200
    And the message sent to queue should contain transaction ID "txn-12345"
