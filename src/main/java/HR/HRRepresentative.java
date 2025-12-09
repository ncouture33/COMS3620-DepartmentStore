package HR;

public class HRRepresentative extends  AbtractHRRepresentative {
    private InformationValidator validator;

    public HRRepresentative(INotificationService notifier, InformationValidator validator){
        super(notifier);
        this.validator = validator;
    }
    @Override
    public void searchEmployee(String lname){
        System.out.println("Searching for employee");
        // search logic 
    }
    @Override
    public void updateEmployee(BaseEmployee employee, String role, double salary, String department){
        System.out.println("Validating updated information...");

        if (!validator.validate(role, salary, department)) {
            notifier.notifyFailure("Invalid employee update information.");
            return;
        }

        System.out.println("Updating employee record...");
        employee.updateRole(role);
        employee.updateSalary(salary);
        employee.updateDepartment(department);

        notifier.notifySuccess("Employee record successfully updated.");
    
    }
}
