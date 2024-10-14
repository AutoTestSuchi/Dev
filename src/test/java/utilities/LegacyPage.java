package utilities;

import java.util.Map;

public class LegacyPage {

    private ExcelUtility excelUtility;

    public LegacyPage(String legacyFileName) {
        excelUtility = new ExcelUtility(legacyFileName);
        excelUtility.loadFile();
    }

    public String getOvertimeData(String employeeID) {
        Map<String, String> employeeData = excelUtility.getEmployeeData(employeeID);
        return employeeData.get("Overtime");
    }
}
