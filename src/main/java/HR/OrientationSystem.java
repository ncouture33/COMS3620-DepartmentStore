package HR;

import HR.Orientation.AbstractOrientationSystem;
import HR.Orientation.BasicOrientationTask;
import HR.Orientation.Orientable;

public class OrientationSystem extends AbstractOrientationSystem {

    @Override
    public void assignRequiredTasks(Orientable employee) {
       }

    @Override
    public void notifyManagerAndHR(OrientableEmployee employee) {
        if (employee.isOrientationComplete()) {
            System.out.println("Orientation complete for " + employee.fName +
                    ". Manager and HR have been notified.");
        }
    }
}
