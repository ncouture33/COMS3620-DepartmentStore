package HR;

import HR.Orientation.Orientable;
import HR.Orientation.OrientationAssignable;
import HR.Orientation.OrientationTask;

public class Manager extends Salary implements OrientationAssignable {

    private String name;

    public Manager(int id, String fName, String lName, int DOB, int social, int salary) {
        super(id, fName, lName, DOB, social, salary);
    }

    @Override
    public void assignTask(Orientable employee, OrientationTask task) {
        employee.addOrientationTask(task);
        System.out.println("Manager " + name + " assigned task: " + task.getTaskName());
    }

}
