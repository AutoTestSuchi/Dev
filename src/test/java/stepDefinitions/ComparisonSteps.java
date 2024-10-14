package stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import pageObjects.WebAppPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utilities.ConfigReader;
import io.cucumber.java.After;
import io.cucumber.java.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ComparisonSteps {

    private Workbook comparisonWorkbook;
    private Workbook legacyWorkbook;
    private ConfigReader configReader;  // For reading configuration
    private String employeeNo;  // Store current employee number
    private double legacyOvertimeHours;
    private double webOvertimeHours;
    private WebDriver driver;
    private WebAppPage webAppPage;  // Page Object for Web App interactions
    // Initialize WebDriver and ConfigReader
    @Before
    public void setUp() {
        try {
            String projectPath = System.getProperty("user.dir");
            System.out.println("Project Path Is: " + projectPath);

            // Set up ChromeDriver with options
            System.setProperty("webdriver.chrome.driver", projectPath + "/src/test/resources/drivers/chromedriver.exe");

            ChromeOptions options = new ChromeOptions();
           options.addArguments("--disable-gpu");  // Helps with stability
            options.addArguments("--no-sandbox");   // Required for running in some environments

            driver = new ChromeDriver(options);  // Initialize ChromeDriver with options
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().window().maximize();  // Maximize the browser window

            configReader = new ConfigReader();  // Initialize ConfigReader to fetch config values
            webAppPage = new WebAppPage(driver);  // Pass the WebDriver instance to WebAppPage

            System.out.println("WebDriver initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing WebDriver.");
        }
    }

    // Step 1: Open the comparison file
    @Given("the comparison file is opened")
    public void openComparisonFile() throws IOException {
        String projectPath = System.getProperty("user.dir");
        String comparefilePath = projectPath + "/src/test/resources/Excel/CompareFile.xlsx"; // Correct file path
        try (FileInputStream fis = new FileInputStream(new File(comparefilePath))) {
            comparisonWorkbook = WorkbookFactory.create(fis);
            System.out.println("Comparison workbook opened successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error opening Excel file: " + comparefilePath);
        }
    }

    private Row matchedRow;  // Define a class-level variable to store the matched row
 // Step 2: Search for employees with variance type
    @When("I search for employee IDs with variance type {string} for the {string} component and {string} element")
    public String searchForEmployeesWithVariance(String varianceType, String component, String element) throws IOException {
      
          Sheet sheet = comparisonWorkbook.getSheetAt(0);  // Assuming the first sheet
        List<String> matchingEmployeeIds = new ArrayList<>();  // To store matching employee IDs

        for (Row row : sheet) {
            Cell componentCell = row.getCell(1);  // Component is in column B
            Cell elementCell = row.getCell(2);    // Element is in Column C
            Cell varianceTypeCell = row.getCell(6);  // Variance Type is in column G

            // Safely retrieve cell values
            String componentValue = getCellValueAsString1(componentCell);
            String elementValue = getCellValueAsString1(elementCell);
            String varianceTypeValue = getCellValueAsString1(varianceTypeCell);

            // Check if all relevant values are non-empty before using trim
            if (!componentValue.isEmpty() && !elementValue.isEmpty() && !varianceTypeValue.isEmpty()) {
                if (componentValue.trim().equalsIgnoreCase(component.trim())
                    && elementValue.trim().equalsIgnoreCase(element.trim())
                    && varianceTypeValue.trim().equalsIgnoreCase(varianceType.trim())) {

                    // Get Employee ID
                    Cell employeeIDCell = row.getCell(0);  // Employee ID in column A
                    String employeeID = getCellValueAsString1(employeeIDCell).trim();  // Get Employee ID as string
                    System.out.println("Employee ID found: " + employeeID);
                    matchingEmployeeIds.add(employeeID);  // Add to the list of matching IDs
                 // Store the matched row in the class-level variable
                    matchedRow = row;  // Store the row for later use
                   // return employeeID;
                }
            
            }
       
        }

        // If no employees were found, print a message
        if (matchingEmployeeIds.isEmpty()) {
            System.out.println("No employees found with variance type '" + varianceType + "' for '" + component + "' component and '" + element + "' element.");
        } else {
            // Process each matching employee (e.g., compare with Legacy and Web application)
            for (String employeeId : matchingEmployeeIds) {
                System.out.println("Processing employee: " + employeeId);
                // Set the current employeeNo for use in further steps
                employeeNo = employeeId;

                // Perform actions for each employee here, such as comparison with Legacy and Web data
                openLegacyFileAndRetrieveData(employeeId);  // Open legacy file and retrieve overtime hours
                logIntoOrangeHRMApplication();  // Log into the OrangeHRM application
                locateOvertimeDataInWebApp(employeeId);  // Locate overtime data in the web app
                compareOvertimeData(employeeId);  // Compare overtime data between legacy and web app
                takeScreenshot();  // Take a screenshot of the web app page
                updateComparisonFile("Comparison.xlsx", employeeId);  // Update the comparison file
                logOutFromOrangeHRM();  // Log out from the web application
            }
        }
		return employeeNo;
    }
 // Helper method to safely retrieve cell values as strings
    private String getCellValueAsString1(Cell cell) {
        if (cell == null) {
            return "";  // Return an empty string if the cell is null
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());  // Convert numeric to string
            default:
                return "";  // Return empty string for other types
        }
    }



    // Step 3: Open the legacy Excel file and retrieve overtime hours
    @And("I open the Legacy file and retrieve the legacy overtime hours data for employee with EmployeeID")
    public void openLegacyFileAndRetrieveData(String employeeID) throws IOException {
    	//String empid = searchForEmployeesWithVariance(String varianceType, String component, String element);
    	String legacyprojectPath = System.getProperty("user.dir");
        String legacyfilePath = legacyprojectPath + "/src/test/resources/Excel/PRTLegacyData.xlsx";

        try (FileInputStream fis = new FileInputStream(new File(legacyfilePath))) {
            legacyWorkbook = WorkbookFactory.create(fis);
            System.out.println(legacyfilePath + " workbook opened successfully.");
            Sheet sheet = legacyWorkbook.getSheetAt(0);  // Assuming first sheet

            for (Row row : sheet) {
                // Get Employee ID as either String or Numeric
                Cell employeeIDCell = row.getCell(0);
                String legacyEmployeeNo = getCellValueAsString1(employeeIDCell);

                if (employeeID.equals(legacyEmployeeNo)) {
                    // Get overtime hours (assuming it's numeric)
                    Cell overtimeHoursCell = row.getCell(1);
                    if (overtimeHoursCell.getCellType() == CellType.NUMERIC) {
                        legacyOvertimeHours = overtimeHoursCell.getNumericCellValue();
                    } else if (overtimeHoursCell.getCellType() == CellType.STRING) {
                        legacyOvertimeHours = Double.parseDouble(overtimeHoursCell.getStringCellValue());
                    }
                    System.out.println("Legacy overtime hours for employee " + employeeID + ": " + legacyOvertimeHours);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error opening Excel file: " + legacyfilePath);
        }
    }

    // Step 4: Log into the OrangeHRM application
    @And("I log into the OrangeHRM application")
    public void logIntoOrangeHRMApplication() {
        try {
            // Get URL, username, and password from the config.properties file
            String url = configReader.getUrl();
            String username = configReader.getUsername();
            String password = configReader.getPassword();

            // Open the OrangeHRM web application and log in using the Page Object methods
            webAppPage.openWebApp(url);  // Open the web app
            webAppPage.loginToWebApp(username, password);  // Log in with credentials

            System.out.println("Logged into OrangeHRM application with credentials from config.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error logging into OrangeHRM application.");
        }
    }

    // Step 5: Locate overtime hours data in the web application
    @And("I locate the overtime hours data for employee with EmployeeID in the web application")
    public void locateOvertimeDataInWebApp(String employeeID) {
        try {
            // Navigate to Attendance and search for employee
            webAppPage.navigateToAttendanceSheets();  // Navigate to Attendance
            webAppPage.searchForEmployee(employeeID);  // Search for employee
      //      webAppPage.clickOnEmployeeFromSearchList(employeeID);  // Select employee from the search result
            webAppPage.clickOnEmployeeRowById(employeeID);

            // Get the overtime data in the format like "1h 26m"
            String overtime = webAppPage.getOvertimeData(employeeID);

            // Convert "1h 26m" format to hours as a double
            double overtimeHours = convertTimeToDecimal(overtime);

            // Set the web overtime hours
            webOvertimeHours = overtimeHours;
            System.out.println("Web application overtime hours for employee " + employeeID + ": " + webOvertimeHours);
        } catch (TimeoutException e) {
            System.out.println("Timeout occurred while trying to locate overtime hours for employee: " + employeeID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to convert "1h 26m" format to a decimal value
    private double convertTimeToDecimal(String timeString) {
        int hours = 0;
        int minutes = 0;

        // Extract hours and minutes using regex
        if (timeString.contains("h")) {
            String[] parts = timeString.split("h");
            hours = Integer.parseInt(parts[0].trim());

            if (parts[1].contains("m")) {
                minutes = Integer.parseInt(parts[1].replace("m", "").trim());
            }
        } else if (timeString.contains("m")) {
            minutes = Integer.parseInt(timeString.replace("m", "").trim());
        }

        // Convert minutes to hours as a fraction
        double totalHours = hours + (minutes / 60.0);
        return totalHours;
    }

    // Step 6: Compare overtime data between the legacy system and web application
    @Then("I compare the overtime data between the legacy system and the web application for employee with EmployeeID")
    public void compareOvertimeData(String employeeID) {
        if (legacyOvertimeHours == webOvertimeHours) {
            System.out.println("Overtime hours match for employee " + employeeID);
        } else {
            System.out.println("Overtime hours mismatch for employee " + employeeID);
            System.out.println("Legacy hours: " + legacyOvertimeHours + ", Web hours: " + webOvertimeHours);
        }
    }

    // Step 7: Take a screenshot of the page and save it
    @And("I take a screenshot of the page and save it")
    public void takeScreenshot() throws IOException {
        // Take a screenshot and save it with a unique name
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String projectPath = System.getProperty("user.dir");
        File destination = new File(projectPath + "/screenshots/screenshot_" + employeeNo + ".png");
        FileUtils.copyFile(screenshot, destination);
        System.out.println("Screenshot saved: " + destination.getPath());
    }

 // Step 8: Update the comparison file with the results
    @And("I update the {string} file with the comparison results for employee with EmployeeID in the Comment section, including the screenshot file name")
    public void updateComparisonFile(String fileName, String employeeID) throws IOException {
        // Ensure the matchedRow is available
        if (matchedRow == null) {
            throw new RuntimeException("No matched row found for employee " + employeeID);
        }

        // Assuming the matched row was stored during searchForEmployeesWithVariance
        Cell commentCell = matchedRow.createCell(7);  // Assuming comments are in column H
        commentCell.setCellValue("Comparison complete. This issue in Oracle value is not loaded correctly. Refer to the Screenshot: screenshot_" + employeeID + ".png");

        // Print to console for confirmation
        System.out.println("Comparison results updated for employee " + employeeID);

        // Save the workbook
        String projectPath = System.getProperty("user.dir");
        String comparefilePath = projectPath + "/src/test/resources/Excel/" + fileName;

        // Open the FileOutputStream and save the workbook
        try (FileOutputStream fos = new FileOutputStream(comparefilePath)) {
            // Ensure you are writing the correct workbook, adjust `comparisonWorkbook` to the actual workbook variable
            comparisonWorkbook.write(fos);
            fos.flush();  // Ensure all data is written to the file
            System.out.println("Excel file updated and saved successfully: " + comparefilePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error saving Excel file: " + comparefilePath, e);
        }
    }

    // Step 9: Log out from the OrangeHRM application
    @And("I log out from the OrangeHRM application")
    public void logOutFromOrangeHRM() {
        webAppPage.logout();  // Use the logout method from WebAppPage
        System.out.println("Logged out from OrangeHRM application.");
    }

    // Tear down method to close the browser after the test
    @After
    public void tearDown() {
        try {
            if (driver != null) {
                driver.quit();  // Quit the browser session
                System.out.println("Browser closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
