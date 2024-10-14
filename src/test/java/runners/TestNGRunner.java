package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepDefinitions", "hooks", "pageObjects"},
    plugin = {"pretty", 
              "html:target/cucumber-reports/cucumber.html", 
              "json:target/cucumber-reports/cucumber.json"},
    monochrome = true,
    tags = "@Test",
    dryRun = false,
    publish = true
)
public class TestNGRunner extends AbstractTestNGCucumberTests {
}
