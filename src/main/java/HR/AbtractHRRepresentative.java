package HR;

public abstract class AbtractHRRepresentative {
    protected  INotificationService notifier;
    public AbtractHRRepresentative (INotificationService notifier){
        this.notifier = notifier;
    } 
    public abstract void searchEmployee(String lname);
    public abstract  void updateEmployee(BaseEmployee employee,String role, double salary, String department);
}
