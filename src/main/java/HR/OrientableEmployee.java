package HR;


import java.util.ArrayList;
import java.util.List;

import HR.Orientation.OrientationTask;

public abstract class OrientableEmployee extends BaseEmployee implements HR.Orientation.Orientable {
    private List<OrientationTask> tasks = new ArrayList<>();
    public OrientableEmployee (int id, String fName, String lName, int DOB, int social, String department, String role) {
       super(id, fName, lName, DOB, social, department, role);
    }
    @Override
    public void addOrientationTask(HR.Orientation.OrientationTask task) {
        tasks.add(task);
        
    }
    
    @Override
    public void completeOrientationTask(String taskName) {
        for (OrientationTask task : tasks) {
            if (task.getTaskName().equalsIgnoreCase(taskName)) {
                task.markCompleted();
                System.out.println(fName + " completed task: " + taskName);
            }
        }
    }
    @Override
    public boolean isOrientationComplete() {
        for (OrientationTask task : tasks) {
            if (!task.isCompleted()) {
                return false;
            }
        }
        return true;
    }
    public List<OrientationTask> getOrientationTasks() {
        return tasks;
    }
    
}
