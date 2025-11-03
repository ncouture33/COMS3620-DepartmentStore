package HR;

public class Hourly extends BaseEmployee{

    private double hourlyRate;
    private double overtimeRate;

    public Hourly(int id, String fName, String lName, int DOB, int social, double hourlyRate, double overtimeRate) {
        super(id, fName, lName, DOB, social);
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
}
