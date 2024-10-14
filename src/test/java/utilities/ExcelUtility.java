package utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ExcelUtility {
    private Workbook workbook;
    private Sheet sheet;
    private String filePath;

    // Maps to store overtime data for employees
    private Map<String, Double> legacyOvertimeDataMap = new HashMap<>();
    private Map<String, Double> webOvertimeDataMap = new HashMap<>();

    public ExcelUtility(String filePath) {
        this.filePath = filePath;
    }

    // Load the Excel file
    public void loadFile() {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);  // Load the first sheet
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Find employee IDs with a specific variance type and component
    public List<String> findEmployeesWithVariance(String varianceType, String component, String element) {
        List<String> employeeIDs = new ArrayList<>();
        for (Row row : sheet) {
            Cell varianceCell = row.getCell(4);  // Assuming variance is in column 5
            Cell componentCell = row.getCell(2); // Assuming component is in column 3
            Cell elementCell = row.getCell(3);   // Assuming element is in column 4

            if (varianceCell != null && varianceType.equals(varianceCell.getStringCellValue())
                    && componentCell.getStringCellValue().equals(component)
                    && elementCell.getStringCellValue().equals(element)) {
                employeeIDs.add(row.getCell(0).getStringCellValue());  // Assuming employee ID is in column 1
            }
        }
        return employeeIDs;
    }

    // Store overtime data from the legacy system
    public void storeLegacyOvertimeData(String employeeID, double overtimeData) {
        legacyOvertimeDataMap.put(employeeID, overtimeData);  // Store legacy overtime data by employee ID
    }

    // Store overtime data from the web system
    public void storeWebOvertimeData(String employeeID, double overtimeData) {
        webOvertimeDataMap.put(employeeID, overtimeData);  // Store web overtime data by employee ID
    }

    // Compare overtime data from legacy and web systems for a specific employee
    public boolean compareOvertimeData(String employeeID) {
        Double legacyOvertime = legacyOvertimeDataMap.get(employeeID);
        Double webOvertime = webOvertimeDataMap.get(employeeID);

        if (legacyOvertime != null && webOvertime != null) {
            return legacyOvertime.equals(webOvertime);  // Return true if the overtime data matches
        }
        return false;
    }

    // Update the comparison Excel file with results and the screenshot file name
    public void updateComparisonFileWithResults(String employeeID, String screenshotFileName) {
        for (Row row : sheet) {
            Cell idCell = row.getCell(0); // Employee ID in column 1
            if (idCell != null && idCell.getStringCellValue().equals(employeeID)) {
                Cell commentCell = row.createCell(7);  // Assuming comment section is in column 8
                commentCell.setCellValue("Comparison complete. Screenshot: " + screenshotFileName);
                break;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to retrieve employee overtime data by employee ID
    public double getEmployeeOvertimeData(String employeeID) {
        for (Row row : sheet) {
            Cell idCell = row.getCell(0);  // Employee ID in column 1
            if (idCell != null && idCell.getStringCellValue().equals(employeeID)) {
                // Assuming overtime data is numeric and in column 5 (adjust the index as needed)
                Cell overtimeCell = row.getCell(4);
                if (overtimeCell != null && overtimeCell.getCellType() == CellType.NUMERIC) {
                    return overtimeCell.getNumericCellValue();
                } else if (overtimeCell != null && overtimeCell.getCellType() == CellType.STRING) {
                    try {
                        return Double.parseDouble(overtimeCell.getStringCellValue());
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing overtime data: " + e.getMessage());
                    }
                }
            }
        }
        return 0.0;  // Default value if no overtime data is found
    }

    // Method to retrieve all employee data by employee ID
    public Map<String, String> getEmployeeData(String employeeID) {
        Map<String, String> employeeData = new HashMap<>();
        for (Row row : sheet) {
            Cell idCell = row.getCell(0); // Assuming employee ID is in column 1
            if (idCell != null && idCell.getStringCellValue().equals(employeeID)) {
                employeeData.put("EmployeeID", idCell.getStringCellValue());

                // Assuming overtime data is in column 5 (adjust the index as needed)
                Cell overtimeCell = row.getCell(4);
                if (overtimeCell != null && overtimeCell.getCellType() == CellType.NUMERIC) {
                    employeeData.put("Overtime", String.valueOf(overtimeCell.getNumericCellValue()));
                } else if (overtimeCell != null && overtimeCell.getCellType() == CellType.STRING) {
                    employeeData.put("Overtime", overtimeCell.getStringCellValue());
                } else {
                    employeeData.put("Overtime", "0.0");
                }
                break;  // Exit loop once employee is found
            }
        }
        return employeeData;
    }
}
