package com.gym.training;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.glue", value = "com.gym.training.steps")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty,html:target/cucumber-report.html")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "@inactiveTrainer")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "@positive and @add")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "@positive or @transaction")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @inactiveTrainer")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "@positive and not @delete")
public class CucumberTestRunner {

}
