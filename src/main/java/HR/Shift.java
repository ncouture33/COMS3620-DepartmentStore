package HR;
import Utils.Data;

public class Shift implements Data{
    private int employeeId;
    private String shiftDate;
    private String startTime;
    private String endTime;
    

    public Shift(int employeeId, String shiftDate, String startTime, String endTime) {
        this.employeeId = employeeId;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public int getEmployeeId() {
        return employeeId;
    }
    public String getShiftDate() {
        return shiftDate;
    }
    public String getStartTime() {
        return startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public String getData(){
        return "\n" + employeeId + " " + shiftDate + " " + startTime + " " + endTime + " ";
    }

}