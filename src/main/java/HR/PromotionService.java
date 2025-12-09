package HR;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

/**
 * Orchestrates the promote/demote use case:
 *  - manager notifies employee
 *  - manager notifies HR
 *  - HR searches for employee
 *  - HR updates employee (role, salary, department)
 *  - event is validated and logged
 *
 * This class is intentionally lightweight so it plugs into your existing HRRepresentative,
 * Manager, InformationValidator and INotificationService implementations.
 */
public class PromotionService {

    private final HRRepresentative hr;
    private final INotificationService notifier;
    private final InformationValidator validator;
    private final String eventLogPath;

    /**
     * @param hr HRRepresentative instance (your concrete HRRepresentative)
     * @param notifier INotificationService implementation (e.g. ConsoleNotifier)
     * @param validator InformationValidator used to validate new role/salary/department
     * @param eventLogPath path to the promotions log file (e.g. "promotions.log")
     */
    public PromotionService(HRRepresentative hr,
                            INotificationService notifier,
                            InformationValidator validator,
                            String eventLogPath) {
        this.hr = hr;
        this.notifier = notifier;
        this.validator = validator;
        this.eventLogPath = eventLogPath;
    }

    /**
     * Perform a promotion/demotion flow.
     *
     * Returns true if the update succeeded and was logged, false otherwise.
     */
    public boolean processPromotion(BaseEmployee employee,
                                    Manager manager,
                                    String newRole,
                                    double newSalary,
                                    String newDepartment,
                                    boolean employeeAccepts) {

        // 1. Manager informs Employee
        manager.notifyEmployee(employee, buildManagerToEmployeeMessage(newRole, newSalary, newDepartment));

        // Alternate flow: employee declines promotion
        if (!employeeAccepts) {
            manager.notifyEmployee(employee, "Employee declined promotion/demotion.");
            // Manager and employee discuss; if later accepted, caller can call processPromotion again
            return false;
        }

        // 2. Manager informs HR
        manager.notifyHR(hr, "Requesting promotion/demotion for employee ID=" + employee.getID()
                + " -> role: " + newRole + ", salary: " + newSalary + ", department: " + newDepartment);

        // 3. HR logs in / searches for employee (search is simulated by your HRRepresentative.searchEmployee)
        hr.searchEmployee(employee.getLName()); // your search API uses last name parameter in many of your files

        // 4. Validate new information before attempting update
        if (!validator.validate(newRole, newSalary, newDepartment)) {
            notifier.notifyFailure("Validation failed for new employee data. Promotion/Demotion aborted.");
            return false;
        }

        // 5. Attempt to update using HRRepresentative's update flow
        try {
            hr.updateEmployee(employee, newRole, newSalary, newDepartment);
        } catch (Exception ex) {
            // If the system fails to update employee information (alternate flow),
            // notify and return false. Caller may retry.
            notifier.notifyFailure("System failed to update employee record: " + ex.getMessage());
            return false;
        }

        // 6. Confirm update & notify
        notifier.notifySuccess("Promotion/Demotion processed for employee ID=" + employee.getID());

        // 7. Log the successful event
        try {
            logEvent(employee, manager, newRole, newSalary, newDepartment, "SUCCESS");
        } catch (IOException ioe) {
            // If log fails, we still consider the update succeeded, but we notify about logging failure.
            notifier.notifyFailure("Employee updated but failed to write promotion log: " + ioe.getMessage());
            return true;
        }

        return true;
    }

    private String buildManagerToEmployeeMessage(String role, double salary, String dept) {
        StringBuilder sb = new StringBuilder();
        sb.append("You have been notified of role change to '").append(role).append("'");
        sb.append(", compensation: ").append(salary);
        sb.append(", department: ").append(dept).append(".");
        return sb.toString();
    }

    private void logEvent(BaseEmployee employee,
                          Manager manager,
                          String role,
                          double salary,
                          String department,
                          String status) throws IOException {

        // Very small append-only log: timestamp | empId | empName | managerId | role | salary | department | status
        try (PrintWriter out = new PrintWriter(new FileWriter(eventLogPath, true))) {
            String line = String.format("%s | EMP_ID=%d | EMP=%s %s | MGR=%s | ROLE=%s | SAL=%.2f | DEPT=%s | %s",
                    Instant.now().toString(),
                    employee.getID(),
                    employee.getFName(),
                    employee.getLName(),
                    (manager.getLName() != null ? manager.getLName() : manager.getID()),
                    role,
                    salary,
                    department,
                    status);
            out.println(line);
        }
    }
}
