package HR;

import java.io.FileWriter;
import java.io.IOException;

import HR.Orientation.Orientable;
import HR.Orientation.OrientationAssignable;
import HR.Orientation.OrientationTask;

public class Manager extends Salary implements OrientationAssignable {

    private String name;

    public Manager(int id, String fName, String lName, int DOB, int social, int salary ,String department, String role) {
        super(id, fName, lName, DOB, social, salary, department, role);
    }

    @Override
    public void assignTask(Orientable employee, OrientationTask task) {
        employee.addOrientationTask(task);
        System.out.println("Manager " + name + " assigned task: " + task.getTaskName());
    }

    void notifyEmployee(BaseEmployee employee, String buildManagerToEmployeeMessage) {
        System.out.println(employee.getFName() +" as been promoted");
    }

    void notifyHR(HRRepresentative hr, String string) {
        System.out.println("Notifying HR   " + string);

    // Optional: append to a file for record-keeping
    try (FileWriter fw = new FileWriter("HRNotifications.txt", true)) {
        fw.write("HR   notified");
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

}
