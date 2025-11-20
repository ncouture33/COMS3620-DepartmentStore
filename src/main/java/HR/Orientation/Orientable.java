package HR.Orientation;

import java.util.List;

public interface Orientable {
    void addOrientationTask(OrientationTask task);
    void completeOrientationTask(String taskName);
    boolean isOrientationComplete();
    List<OrientationTask> getOrientationTasks();
}
