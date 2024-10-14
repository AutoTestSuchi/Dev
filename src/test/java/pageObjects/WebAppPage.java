package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

public class WebAppPage {
    WebDriver driver;
    WebDriverWait wait;

    // Constructor to initialize WebDriver and WebDriverWait
    public WebAppPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));  // Wait for 10 seconds
    }

    // Locators for login
    By usernameField = By.id("txtUsername");
    By passwordField = By.id("txtPassword");
    By loginButton = By.xpath("//button[@type='submit']");

    // Locators for navigation after login
    By attendanceMenu = By.xpath("//li[@id='left_menu_item_23']//a[1]//span[1]");
    By employeeNameField = By.xpath("//oxd-multiselect[@id='report_multiselect_empfilter_employee_name']//div//input[@placeholder='Type for hints...']");
    By employeesearchlist = By.xpath("//span[@class='multi-select-title']");
    By employeenotsubmitbutton =By.xpath("//tbody/tr[1]/td[8]/a[1]");
    By overtimeHours = By.xpath("//div[@class='pay-hours-duration pay-hours-duration-2']");
    By logOutButton = By.xpath("//li[@id='navbar-logout']//a[@href='/auth/logout']");
	private WebElement attendanceTable;

    
    
    // Open the OrangeHRM web application
    public void openWebApp(String url) {
        driver.get(url);
    }

    // Login to the OrangeHRM web application
    public void loginToWebApp(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField)).sendKeys(username);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();
    }

    // Navigate to Attendance Sheets page
    public void navigateToAttendanceSheets() {
        WebElement attendanceMenuElement = wait.until(ExpectedConditions.elementToBeClickable(attendanceMenu));
        attendanceMenuElement.click();
        System.out.println("Clicked on Attendance menu.");
    }

//  Search for an employee by their Employee ID in the Attendance Sheets
public void searchForEmployee(String employeeID) {
    // Wait for the input field to be visible and ready
    WebElement employeeInputField = wait.until(ExpectedConditions.visibilityOfElementLocated(employeeNameField));
    
    // Clear the input field before entering the employee ID
    employeeInputField.clear();
    
    // Enter the employee ID
    employeeInputField.sendKeys(employeeID);

    // Wait for the dropdown or suggestions to appear (you need a suitable locator for the suggestions dropdown)
    WebElement suggestionElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//xpath-to-suggestion-element")));

    // Use Actions to select the suggestion (if the suggestions dropdown appears)
    Actions actions = new Actions(driver);
    actions.moveToElement(suggestionElement).sendKeys(Keys.ARROW_DOWN).perform();
    actions.sendKeys(Keys.ENTER).perform();  // Press "Enter" to select the employee
    
    System.out.println("Selected employee with ID: " + employeeID);
}


//public void clickOnEmployeeFromSearchList(String employeeID) throws TimeoutException {
//    try {
//        // Define the XPath to locate the specific employee in the dropdown by employeeID
//        String employeeXPath = "//a[.//span[contains(@class, 'multi-select-employee-id') and text()='" + employeeID + "']]";
//        
//        // Wait until the employee with the matching ID appears in the search list
//        WebElement employeeElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(employeeXPath)));
//        
//        // Click on the employee ID from the search list
//        employeeElement.click();
//        System.out.println("Clicked on the employee with ID: " + employeeID);
//    } catch (NoSuchElementException e) {
//        System.err.println("Employee with ID " + employeeID + " not found in the DOM.");
//        throw e;
//    } catch (Exception e) {
//        System.err.println("Unexpected error while trying to click on employee ID: " + employeeID);
//        e.printStackTrace();
//        throw new RuntimeException("Error clicking on the employee with ID: " + employeeID, e);
//    }
//}


 // Search for an employee by their Employee ID in the Attendance Sheets and click on the "Not Submit" button in the row
    public void clickOnEmployeeRowById(String employeeID) throws TimeoutException {
        try {
            // Define the XPath to locate the row that contains the matching employee ID
         //   String xpathForEmployeeRow = "//a[@href='#/attendance/attendance_sheet/1986///' and text()" + employeeID + "')]]";
            
                     
            // Wait until the table row with the matching employee ID is visible
       //     WebElement employeeRow = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathForEmployeeRow)));
       //    employeeRow.click();
            
         // Using XPath to locate the element by its text '1080'
            WebElement employeeLink = driver.findElement(By.xpath("//a[text()='1080']"));

            // Optionally, scroll into view if needed
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", employeeLink);

            // Click the employee link
            employeeLink.click();

            
            
            System.out.println("Clicked on the 'Not Submit' button for Employee ID: " + employeeID);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error clicking on 'Not Submit' button for Employee ID: " + employeeID);
        }
    }

    // Locate and return the Extra Time (Overtime) data from the result table for the employee
    public String getOvertimeData(String employeeID) {
    	// Wait until the overtime field is visible
        WebElement overtimeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(overtimeHours));
         // Get the text value of the overtime field
        String overtimeHours = overtimeElement.getText();
       
        // Print the overtime hours
        System.out.println("OverTime Hours displayed in Application: " + overtimeHours);
        
        // Return the overtime hours
        return overtimeHours;
    }

    // Logout from the web application
    public void logout() {
        driver.findElement(logOutButton).click();
    }
}
