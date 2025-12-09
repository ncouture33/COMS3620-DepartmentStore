package StoreFloor;

public class Tailor extends AbstractPerson {
    private final String employeeId;

    public Tailor(String name, String employeeId) {
        super(name);
        this.employeeId = employeeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getData() {
        return name + " " + employeeId;
    }
}
