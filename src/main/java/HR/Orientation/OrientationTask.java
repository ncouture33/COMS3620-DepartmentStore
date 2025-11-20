package HR.Orientation;

public interface OrientationTask {
    String getTaskName();
    String getTaskDescription();
    boolean isCompleted();
    void markCompleted();
}
