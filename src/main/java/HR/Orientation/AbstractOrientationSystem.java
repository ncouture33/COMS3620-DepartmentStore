package HR.Orientation;

import HR.OrientableEmployee;

public abstract class AbstractOrientationSystem {
    public abstract void assignRequiredTasks(Orientable employee);
    public abstract void notifyManagerAndHR(OrientableEmployee employee);
}
