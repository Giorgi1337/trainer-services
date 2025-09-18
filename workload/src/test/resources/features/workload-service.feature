Feature: Workload Service Component Tests
  As a workload service
  I want to process training workload updates and provide trainer summaries
  So that trainer workload data can be stored and retrieved efficiently

  Background:
    Given the workload service is running
    And the MongoDB database is available
    And the database is clean

  @positive @add @newTrainer
  Scenario: Successfully process ADD operation for new trainer
    When I process a workload update:
      | trainerUsername  | john.doe           |
      | trainerFirstName | John               |
      | trainerLastName  | Doe                |
      | active           | true               |
      | trainingDate     | 2024-12-15T10:00:00|
      | trainingDuration | 60                 |
      | actionType       | ADD                |
    Then no exception should be thrown
    And the trainer document should be created in database
    And the trainer document should contain:
      | username   | john.doe |
      | firstName  | John     |
      | lastName   | Doe      |
      | active     | true     |
    And the workload for year 2024 month 12 should be 60 minutes

  @positive @add @existingTrainer
  Scenario: Successfully process ADD operation for existing trainer
    Given a trainer "jane.smith" with existing workload data:
      | firstName        | Jane     |
      | lastName         | Smith    |
      | active           | true     |
      | year             | 2024     |
      | month            | 11       |
      | existingDuration | 120      |
    When I process a workload update:
      | trainerUsername  | jane.smith         |
      | trainerFirstName | Jane               |
      | trainerLastName  | Smith              |
      | active           | true               |
      | trainingDate     | 2024-11-20T14:00:00|
      | trainingDuration | 45                 |
      | actionType       | ADD                |
    Then no exception should be thrown
    And the workload for year 2024 month 11 should be 165 minutes

  @positive @add @newMonth
  Scenario: Successfully add workload to new month for existing trainer
    Given a trainer "bob.wilson" with existing workload data:
      | firstName        | Bob      |
      | lastName         | Wilson   |
      | active           | true     |
      | year             | 2024     |
      | month            | 10       |
      | existingDuration | 90       |
    When I process a workload update:
      | trainerUsername  | bob.wilson         |
      | trainerFirstName | Bob                |
      | trainerLastName  | Wilson             |
      | active           | true               |
      | trainingDate     | 2024-11-15T09:00:00|
      | trainingDuration | 75                 |
      | actionType       | ADD                |
    Then no exception should be thrown
    And the workload for year 2024 month 10 should be 90 minutes
    And the workload for year 2024 month 11 should be 75 minutes

  @positive @delete
  Scenario: Successfully process DELETE operation with sufficient existing duration
    Given a trainer "alice.brown" with existing workload data:
      | firstName        | Alice    |
      | lastName         | Brown    |
      | active           | true     |
      | year             | 2024     |
      | month            | 12       |
      | existingDuration | 180      |
    When I process a workload update:
      | trainerUsername  | alice.brown        |
      | trainerFirstName | Alice              |
      | trainerLastName  | Brown              |
      | active           | true               |
      | trainingDate     | 2024-12-10T16:00:00|
      | trainingDuration | 60                 |
      | actionType       | DELETE             |
    Then no exception should be thrown
    And the workload for year 2024 month 12 should be 120 minutes

  @positive @delete @negativeLimit
  Scenario: Successfully process DELETE operation with duration exceeding existing (should not go below 0)
    Given a trainer "charlie.davis" with existing workload data:
      | firstName        | Charlie  |
      | lastName         | Davis    |
      | active           | true     |
      | year             | 2024     |
      | month            | 12       |
      | existingDuration | 30       |
    When I process a workload update:
      | trainerUsername  | charlie.davis      |
      | trainerFirstName | Charlie            |
      | trainerLastName  | Davis              |
      | active           | true               |
      | trainingDate     | 2024-12-05T11:00:00|
      | trainingDuration | 60                 |
      | actionType       | DELETE             |
    Then no exception should be thrown
    And the minimum duration should be enforced to 0

  @positive @delete @deleteOperation
  Scenario: Successfully process DELETE operation for new trainer (creates with 0 duration)
    When I process a workload update:
      | trainerUsername  | new.trainer        |
      | trainerFirstName | New                |
      | trainerLastName  | Trainer            |
      | active           | true               |
      | trainingDate     | 2024-12-01T13:00:00|
      | trainingDuration | 30                 |
      | actionType       | DELETE             |
    Then no exception should be thrown
    And the trainer document should be created in database
    And the workload for year 2024 month 12 should be 0 minutes

  @positive @inactive
  Scenario: Successfully process workload for inactive trainer
    When I process a workload update:
      | trainerUsername  | inactive.trainer   |
      | trainerFirstName | Inactive           |
      | trainerLastName  | Trainer            |
      | active           | false              |
      | trainingDate     | 2024-12-20T08:00:00|
      | trainingDuration | 45                 |
      | actionType       | ADD                |
    Then no exception should be thrown
    And the trainer document should contain:
      | username   | inactive.trainer |
      | firstName  | Inactive         |
      | lastName   | Trainer          |
      | active     | false            |
    And the workload for year 2024 month 12 should be 45 minutes

  @positive @transaction
  Scenario: Process workload update with transaction ID propagation
    When I process a workload update with transaction ID "txn-98765":
      | trainerUsername  | john.doe           |
      | trainerFirstName | John               |
      | trainerLastName  | Doe                |
      | active           | true               |
      | trainingDate     | 2024-12-25T15:00:00|
      | trainingDuration | 90                 |
      | actionType       | ADD                |
    Then no exception should be thrown
    And the trainer document should be created in database
    And the workload for year 2024 month 12 should be 90 minutes

  @positive @summary @existing
  Scenario: Successfully retrieve trainer summary for existing trainer
    Given a trainer "summary.test" with existing workload data:
      | firstName        | Summary  |
      | lastName         | Test     |
      | active           | true     |
      | year             | 2024     |
      | month            | 11       |
      | existingDuration | 240      |
    When I request trainer summary for username "summary.test"
    Then the trainer summary should be returned
    And the trainer summary should contain:
      | username   | summary.test |
      | firstName  | Summary      |
      | lastName   | Test         |
      | active     | true         |
    And the trainer summary should contain year 2024 with total months 1
    And the year 2024 should contain month 11 with duration 240

  @negative @summary @notFound
  Scenario: Request trainer summary for non-existing trainer
    When I request trainer summary for username "non.existing.trainer"
    Then the trainer summary should be null

  @positive @summary @multipleMonths
  Scenario: Retrieve trainer summary with multiple months of data
    Given a trainer "multi.month" with existing workload data:
      | firstName        | Multi    |
      | lastName         | Month    |
      | active           | true     |
      | year             | 2024     |
      | month            | 10       |
      | existingDuration | 150      |
    When I process a workload update:
      | trainerUsername  | multi.month        |
      | trainerFirstName | Multi              |
      | trainerLastName  | Month              |
      | active           | true               |
      | trainingDate     | 2024-11-15T12:00:00|
      | trainingDuration | 90                 |
      | actionType       | ADD                |
    And I process a workload update:
      | trainerUsername  | multi.month        |
      | trainerFirstName | Multi              |
      | trainerLastName  | Month              |
      | active           | true               |
      | trainingDate     | 2024-12-10T14:00:00|
      | trainingDuration | 120                |
      | actionType       | ADD                |
    And I request trainer summary for username "multi.month"
    Then the trainer summary should be returned
    And the trainer summary should contain year 2024 with total months 3
    And the year 2024 should contain month 10 with duration 150
    And the year 2024 should contain month 11 with duration 90
    And the year 2024 should contain month 12 with duration 120

  @positive @update @trainerInfo
  Scenario: Update trainer information during workload processing
    Given a trainer "update.test" with existing workload data:
      | firstName        | Old      |
      | lastName         | Name     |
      | active           | false    |
      | year             | 2024     |
      | month            | 11       |
      | existingDuration | 60       |
    When I process a workload update:
      | trainerUsername  | update.test        |
      | trainerFirstName | New                |
      | trainerLastName  | Name               |
      | active           | true               |
      | trainingDate     | 2024-11-20T10:00:00|
      | trainingDuration | 30                 |
      | actionType       | ADD                |
    Then no exception should be thrown
    And the trainer document should contain:
      | username   | update.test |
      | firstName  | New         |
      | lastName   | Name        |
      | active     | true        |
    And the workload for year 2024 month 11 should be 90 minutes