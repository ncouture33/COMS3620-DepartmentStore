package HR;

public class Hourly extends BaseEmployee{

    private double hourlyRate;
    private double overtimeRate;

    public Hourly(int id, String fName, String lName, int DOB, int social, double hourlyRate, double overtimeRate, String department, String role) {
        super(id, fName, lName, DOB, social, department, role);
        this.hourlyRate = hourlyRate;
        this.overtimeRate = overtimeRate;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getOvertimeRate() {
        return overtimeRate;
    }

    public void setOvertimeRate(double overtimeRate) {
        this.overtimeRate = overtimeRate;
    }

    @Override
    public double renderPayment() {
        return card.getHoursWorked() * hourlyRate + card.getOvertimeHours() * overtimeRate;
    }

    @Override
    public void resetTimeCard() {
        card = new TimeCard();
    }

    public String toString(){
        return "HOURLY, " + super.toString() + ", Hourly Rate: " + hourlyRate + ", Overtime Rate: " + overtimeRate;
    }
    public String getData(){
        return "HOURLY " + super.getData() + hourlyRate + " " + overtimeRate + " ";
    }
    @Override
    public void updateSalary(double newSalary){
        this.hourlyRate = newSalary;
    }
}
