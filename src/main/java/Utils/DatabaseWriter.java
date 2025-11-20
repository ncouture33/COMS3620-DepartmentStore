package Utils;

import HR.BaseEmployee;
import HR.OffboardingEmployee;
import HR.Payroll;
import HR.Paystub;
import StoreOperations.ClockTime;

import java.util.ArrayList;

public interface DatabaseWriter {
    public void writeEmployee(BaseEmployee data);

    public void indicateEmployeeOffboarding(int empId, String date, String reasonForLeaving);

    public void writePriorEmployee(OffboardingEmployee emp, String propertyReturned);

    public void writeClockedInEmployee(BaseEmployee emp, String time, int date);

    public void writeTimeHistory(ClockTime emp);

    public ArrayList<ClockTime> getClockedEmployees();

    public void clockOutEmployee(ClockTime emp);

    public ArrayList<BaseEmployee> getEmployees();

    public OffboardingEmployee getOffboardingEmployee(int empID);

    public void removeOffboardingEmployee(int empID);

    public void removeFromEmployee(int empID);

    public void addPayroll();

    public Payroll getPayroll();

    public void writePaystubs(ArrayList<Paystub> list, String date);

}
