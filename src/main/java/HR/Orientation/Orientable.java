package HR.Orientation;

public interface Orientable {
    void addOrientationTask(OrientationTask task);
    void completeOrientationTask(String task);
    boolean isOrientationComplete();
}
