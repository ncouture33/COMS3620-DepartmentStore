package HR;
import Utils.Data;

public class TimeCard implements Data{
    private int timePeriod;
    private double hoursWorked;
    private double overtimeHours;

    public TimeCard() {
        this.timePeriod = 0;
        this.hoursWorked = 0;
        this.overtimeHours = 0;
    }

    public TimeCard(int timePeriod, double hoursWorked, double overtimeHours) {
        this.timePeriod = timePeriod;
        this.hoursWorked = hoursWorked;
        this.overtimeHours = overtimeHours;
    }

    //Returns time period in weeks
    public int getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void increaseHoursWorked(double hours) {
        if (hoursWorked + hours > 40){
            double extraHours = (hoursWorked + hours) - 40;
            hoursWorked = 40;
            overtimeHours += extraHours;
        } else {
            hoursWorked += hours;
        }
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    @Override
    public String toString(){
        return "Time Period (weeks): " + timePeriod + ", Hours Worked: " + hoursWorked + ", Overtime Hours: " + overtimeHours + " ";
    }

    public String getData(){
        return timePeriod + " " + hoursWorked + " " + overtimeHours + " ";
    }
}
