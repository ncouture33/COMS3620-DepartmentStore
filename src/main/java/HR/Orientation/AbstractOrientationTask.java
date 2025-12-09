package HR.Orientation;

public abstract class AbstractOrientationTask  implements OrientationTask {
    protected String taskName;
    protected String taskDescription;
    protected boolean completed;

    public AbstractOrientationTask(String taskName, String taskDescription) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.completed = false;
    }

    @Override
    public void markCompleted() {
        this.completed = true;
    }
    @Override
    public boolean isCompleted() {
        return completed;
    }
    @Override
    public String getTaskName() {
        return taskName;
    }
    @Override
    public String getTaskDescription() {
        return taskDescription;
    }
}
