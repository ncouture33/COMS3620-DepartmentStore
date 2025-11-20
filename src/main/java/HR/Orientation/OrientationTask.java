package HR.Orientation;

public interface OrientationTask {
    String getTaskName();
    String getTaskDescription();
    void markCompleted();
    boolean isCompleted();
}
