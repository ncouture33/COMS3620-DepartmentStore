package Utils;

import java.util.ArrayList;

import HR.BaseEmployee;
import HR.OffboardingEmployee;
import HR.Orientation.OrientationTask;
import HR.Payroll;
import HR.Paystub;
import HR.Schedule;
import HR.Timeoff;
import StoreOperations.ClockTime;
import StoreFloor.Customer;
import StoreFloor.Rewards;

public interface DatabaseWriter {
    public void writeEmployee(BaseEmployee data);

    public void indicateEmployeeOffboarding(int empId, String date, String reasonForLeaving);

    public void writePriorEmployee(OffboardingEmployee emp, String propertyReturned);

    public void writeClockedInEmployee(BaseEmployee emp, String time, int date);

    public void writeTimeHistory(ClockTime emp);

    public ArrayList<ClockTime> getClockedEmployees();

    public boolean clockOutEmployee(ClockTime emp);

    public ArrayList<BaseEmployee> getEmployees();

    public OffboardingEmployee getOffboardingEmployee(int empID);

    public void removeOffboardingEmployee(int empID);

    public void removeFromEmployee(int empID);

    public void addPayroll();

    public Payroll getPayroll();

    public void writePaystubs(ArrayList<Paystub> list, String date);

    public ArrayList<BaseEmployee> getAllEmployeesExcluding(ArrayList<Integer> excludeList);

    public ArrayList<Timeoff> getTimeoffs();

    public void writeTimeoff(Timeoff data);

    public int getNextScheduleID();

    public void writeSchedule(Schedule schedule);

    public void addOrientationTask(int empId, String taskName, String taskDescription);
    public ArrayList<OrientationTask> getOrientationTasks(int empId);
    public boolean completeOrientationTask(int empId, String taskName);

    public void addCustomerToRewardsProgram(Customer customer);

    public Rewards getCustomerRewards(String phoneNumber);

    public int generateCustomerRewardsID();

    public void updateCustomerRewardsPoints(Rewards rewards);
}
