package HR;

import java.util.ArrayList;
import Utils.Data;

public class Timeoff implements Data{
    private final int employeeId;
    private ArrayList<String> dates;

    public Timeoff(int employeeId){
        this.employeeId = employeeId;
        dates = new ArrayList<String>();
    }

    public int getEmployeeId(){
        return this.employeeId;
    }

    public void addDate(String date){
        this.dates.add(date);
    }

    public ArrayList<String> getDates(){
        return this.dates;
    }

    public int countTimeoffDays(){
        return this.dates.size();
    }

    public String getData(){
        StringBuilder sb = new StringBuilder();
        sb.append(employeeId).append(" ");
        for(String date : dates){
            sb.append(date).append(" ");
        }
        return sb.toString();
    }
}