package com.gym.workload;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.glue", value = "com.gym.workload.steps")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty,html:target/cucumber-report.html")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "@deleteOperation")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "@positive and @add")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "@positive or @transaction")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @negativeLimit")
//@ConfigurationParameter(key = "cucumber.filter.tags", value = "@positive and not @summary")
public class CucumberTestRunner {
}