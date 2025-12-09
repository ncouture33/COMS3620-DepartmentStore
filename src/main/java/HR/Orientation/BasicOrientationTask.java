package HR.Orientation;

public class BasicOrientationTask implements OrientationTask {
    private String name;
    private String description;
    private boolean completed = false;

    public BasicOrientationTask(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getTaskName() {
        return name;
    }

    @Override
    public String getTaskDescription() {
        return description;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void markCompleted() {
        this.completed = true;
    }
}
