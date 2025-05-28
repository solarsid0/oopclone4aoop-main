package oop.test;

import CSV.CSVDatabaseProcessor;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import oop.classes.actors.User;
import oop.classes.empselfservice.AttendanceDetails;
import oop.classes.management.UserAuthentication;

/**
 * Test class that calculates employee's attendance details for a pay period.
 * Uses credentials to identify the employee and fetch their attendance records.
 */
public class AttendanceDetailsTest {

    public static void main(String[] args) {
        // Initialize the CSV database processor
        CSVDatabaseProcessor csvProcessor = new CSVDatabaseProcessor();

        try {
            // Load necessary data
            System.out.println("Loading user credential data...");
            csvProcessor.loadUserCredentialData();

            System.out.println("Loading attendance data...");
            csvProcessor.loadAttendanceData();

            // Verify data was loaded successfully
            if (csvProcessor.getAllUserCredentialRecords() == null
                    || csvProcessor.getAllUserCredentialRecords().isEmpty()) {
                System.out.println("Error: Failed to load user credential data!");
                return;
            }

            // Test attendance data retrieval and processing
            testAttendanceDataRetrieval(csvProcessor);

            // Optional: User login process for interactive testing
            UserAuthentication auth = new UserAuthentication(csvProcessor);
            Scanner scanner = new Scanner(System.in);
            boolean isLoggedIn = false;
            String employeeId = null;
            String firstName = null;
            String lastName = null;

            while (!isLoggedIn) {
                System.out.println("=== Employee Attendance System ===");
                System.out.print("Enter email: ");
                String email = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                // Validate credentials
                User user = auth.validateCredentials(email, password);

                if (user != null) {
                    // Get employee info directly from the credentials file
                    Map<String, String> employeeInfo = getEmployeeInfoFromEmail(csvProcessor, email);

                    if (employeeInfo != null) {
                        employeeId = employeeInfo.get("Employee ID");
                        firstName = employeeInfo.get("First Name");
                        lastName = employeeInfo.get("Last Name");

                        isLoggedIn = true;
                        System.out.println("Login successful!");
                        System.out.println("Welcome, " + firstName + " " + lastName + " (ID: " + employeeId + ")");
                    } else {
                        System.out.println("Employee profile not found. Please contact administrator.");
                    }
                } else {
                    System.out.println("Invalid credentials. Please try again.");
                }
            }

            // Once logged in, ask for the month to calculate attendance
            System.out.println("\nSelect month for attendance report:");
            int currentMonth = LocalDate.now().getMonthValue();
            int currentYear = LocalDate.now().getYear();

            for (int i = 1; i <= 12; i++) {
                System.out.println(i + ". " + Month.of(i));
            }

            System.out.print("Enter month number (1-12): ");
            int selectedMonth = Integer.parseInt(scanner.nextLine());

            // Default set to current year, but allow user to select a different year
            System.out.print("Enter year (default " + currentYear + "): ");
            String yearInput = scanner.nextLine();
            int selectedYear = yearInput.isEmpty() ? currentYear : Integer.parseInt(yearInput);

            // Calculate the date range for the selected month
            YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            // Generate the attendance report
            generateMonthlyAttendanceReport(csvProcessor, employeeId, firstName, lastName, startDate, endDate);

            scanner.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests attendance data retrieval and processing using CSVDatabaseProcessor.
     */
    private static void testAttendanceDataRetrieval(CSVDatabaseProcessor csvProcessor) {
        System.out.println("\n=== Testing Attendance Data Retrieval ===");

        // Fetch an employee ID from the CSV
        List<Map<String, String>> userCredentials = csvProcessor.getAllUserCredentialRecords();
        if (userCredentials == null || userCredentials.isEmpty()) {
            System.out.println("No user credentials found to test attendance retrieval.");
            return;
        }

        String testEmployeeId = userCredentials.get(0).get("Employee ID"); // Get first employee ID
        LocalDate testDate = LocalDate.of(2024, 1, 1); // Replace with a valid date from your CSV

        AttendanceDetails attendanceDetails = AttendanceDetails.getAttendanceDetailsByEmployeeIdAndDate(
                csvProcessor, testEmployeeId, testDate);

        if (attendanceDetails.getDate() != null) {
            System.out.println("\nAttendance details for Employee ID " + testEmployeeId + " on " + testDate + ":");
            System.out.println("Log In: " + attendanceDetails.getLogIn());
            System.out.println("Log Out: " + attendanceDetails.getLogOut());
            System.out.println("Late Hours: " + attendanceDetails.getLateHours());
            System.out.println("Hours Worked: " + attendanceDetails.getHoursWorked());
            System.out.println("Overtime Hours: " + attendanceDetails.getOvertimeHours());
        } else {
            System.out.println("\nNo attendance record found for Employee ID " + testEmployeeId + " on " + testDate);
        }
    }

    /**
     * Retrieves the employee information associated with the provided email address.
     */
    private static Map<String, String> getEmployeeInfoFromEmail(CSVDatabaseProcessor csvProcessor, String email) {
        List<Map<String, String>> userCredentials = csvProcessor.getAllUserCredentialRecords();

        if (userCredentials == null || userCredentials.isEmpty()) {
            System.out.println("No credential records found.");
            return null;
        }

        for (Map<String, String> record : userCredentials) {
            String recordEmail = record.get("Email");
            if (email.equals(recordEmail)) {
                return record;
            }
        }

        System.out.println("No matching email found in credential records.");
        return null;
    }

    /**
     * Generates a monthly attendance report for an employee.
     */
    private static void generateMonthlyAttendanceReport(
            CSVDatabaseProcessor csvProcessor,
            String employeeId,
            String firstName,
            String lastName,
            LocalDate startDate,
            LocalDate endDate) {

        System.out.println("\n=== Monthly Attendance Report ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Employee Name: " + firstName + " " + lastName);
        System.out.println("Period: " + startDate + " to " + endDate);

        // Check if the processor has attendance records
        System.out.println("Checking attendance records...");
        List<Map<String, Object>> attendanceRecords = csvProcessor.getAttendanceRecordsByEmployeeId(employeeId);

        if (attendanceRecords == null || attendanceRecords.isEmpty()) {
            System.out.println("No attendance records found for employee ID: " + employeeId);
            System.out.println("Please check if attendance data is properly loaded.");
            return;
        }

        System.out.println("Found " + attendanceRecords.size() + " attendance records for employee.");

        // Collect attendance details for each day in the period
        List<AttendanceDetails> attendanceList = new ArrayList<>();
        LocalDate currentDate = startDate;

        System.out.println("\nGathering attendance details for the selected period...");
        while (!currentDate.isAfter(endDate)) {
            AttendanceDetails dailyAttendance = AttendanceDetails.getAttendanceDetailsByEmployeeIdAndDate(
                    csvProcessor, employeeId, currentDate);

            if (dailyAttendance.getDate() != null) {  // Only add if there's an actual record
                attendanceList.add(dailyAttendance);
                System.out.println("Found record for: " + currentDate);
            }

            currentDate = currentDate.plusDays(1);
        }

        // If no attendance records found in the period
        if (attendanceList.isEmpty()) {
            System.out.println("No attendance records found for the selected period.");
            return;
        }

        // Calculate the summary metrics
        int daysWorked = AttendanceDetails.calculateDaysWorked(csvProcessor, employeeId, startDate, endDate);
        double totalHoursWorked = 0;
        double totalLateHours = 0;
        double totalOvertimeHours = 0;

        for (AttendanceDetails attendance : attendanceList) {
            totalHoursWorked += attendance.getHoursWorked();
            totalLateHours += attendance.getLateHours();
            totalOvertimeHours += attendance.getOvertimeHours();
        }

        System.out.println("\n=== Attendance Summary ===");
        System.out.println("Total Days Worked: " + daysWorked);
        System.out.println("Total Hours Worked: " + totalHoursWorked);
        System.out.println("Total Late Hours: " + totalLateHours);
        System.out.println("Total Overtime Hours: " + totalOvertimeHours);
    }
}
