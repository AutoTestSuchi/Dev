@Test
  
Feature: Compare Overtime Earning Component Data between Legacy and Web Application

  Scenario: Compare Overtime Hours data for employees with variance type "Not Matched" for the "Earning" component and "Overtime Pay" element
    Given the comparison file is opened
    When I search for employee IDs with variance type "Not Matched" for the "Earning" component and "Overtime Pay" element
    And I open the Legacy file and retrieve the legacy overtime hours data for employee with EmployeeID
    And I log into the OrangeHRM application
    And I locate the overtime hours data for employee with EmployeeID in the web application
    Then I compare the overtime data between the legacy system and the web application for employee with EmployeeID
    And I take a screenshot of the page and save it
    And I update the "Comparison.xlsx" file with the comparison results for employee with EmployeeID in the Comment section, including the screenshot file name
    And I log out from the OrangeHRM application
    