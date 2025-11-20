package HR;
import java.util.ArrayList;

import Utils.Data;

public class Schedule implements Data {
    private int scheduleId;
    private int hours;
    private int shiftLength;
    private int minimumRequiredStaff;
    private int timePeriodInDays;
    private ArrayList<Shift> shifts;

    public Schedule(int scheduleId, int hours, int shiftLength, int minimumRequiredStaff){
        this.scheduleId = scheduleId;
        this.hours = hours;
        this.shiftLength = shiftLength;
        this.minimumRequiredStaff = minimumRequiredStaff;
        this.timePeriodInDays = 14; // Assuming shifts cover full days
        this.shifts = new ArrayList<Shift>();
    }
    
    public int getScheduleId(){
        return this.scheduleId;
    }
    
    public void determineSchedule(ArrayList<BaseEmployee> employees, ArrayList<Timeoff> timeoffs){
        //make a list of employees excluding those on timeoff
        //then sort the list so that ones with time off have priorities to be scheduled first
        sortEmployeesByPriority(employees, timeoffs);
        int[] assignedHours = new int[employees.size()];
        for(int i = 0; i < timePeriodInDays; i++){
            for(int j = 0; j < hours / shiftLength; j++){
                int staffCount = 0;
                for (BaseEmployee employee : employees){
                    if(staffCount >= minimumRequiredStaff){
                        break;
                    }
                    if(isEmployeeAvailableForShift(employee, i, timeoffs, assignedHours, employees.indexOf(employee))){
                        assignEmployeeToShift(employee, i, j);
                        assignedHours[employees.indexOf(employee)] = assignedHours[employees.indexOf(employee)] + shiftLength;
                        staffCount++;
                    }
                }
                if (staffCount < this.minimumRequiredStaff) {
                    System.out.println("Warning: Not enough staff available for day " + (i+1) + ", shift " + (j+1));
                }
            }
        }
    }

    public void addShift(Shift shift){
        this.shifts.add(shift);
    }

    private boolean isEmployeeAvailableForShift(BaseEmployee employee, int day, ArrayList<Timeoff> timeoffs, int[] assignedHours, int assignedHoursIndex){
        //Temporary date tracking implementation
        String shiftDate = "2025-11-" + String.format("%02d", day + 1); // Example date format
        for(Timeoff to : timeoffs){
            if(to.getEmployeeId() == employee.getID()){
                if(to.getDates().contains(shiftDate)){
                    return false;
                }
            }
        }

        //Also check if assigning this shift would exceed their max hours
        if(assignedHours[assignedHoursIndex] + shiftLength > getMaxHoursPerWeek()){
            return false;
        }

        return true;
    }

    private int getMaxHoursPerWeek(){
        //placeholder value
        return 45; 
    }

    private void assignEmployeeToShift(BaseEmployee employee, int day, int shiftIndex){
        String shiftDate = "2025-11-" + String.format("%02d", day + 1); // Example date format
        String startTime = String.format("%02d:00", shiftIndex * shiftLength);
        String endTime = String.format("%02d:00", (shiftIndex + 1) * shiftLength);
        Shift shift = new Shift(employee.getID(), shiftDate, startTime, endTime);
        shifts.add(shift);
    }
    
    private void sortEmployeesByPriority(ArrayList<BaseEmployee> employees, ArrayList<Timeoff> timeoffs){
        //want to sort so that employees with time off requests are prioritized - so they come first basically
        int n = employees.size();
        boolean swapped = false;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                int nto = numberOfTimeoffs(employees.get(j), timeoffs);
                int nto1 = numberOfTimeoffs(employees.get(j + 1), timeoffs);
                if (nto < nto1) {
                    // swap employees[j] and employees[j+1]
                    BaseEmployee temp = employees.get(j);
                    employees.set(j, employees.get(j + 1));
                    employees.set(j + 1, temp);
                    swapped = true;
                }
            }
        
            // If no two elements were swapped, then break
            if (!swapped)
                break;
        }
    }

    private int numberOfTimeoffs(BaseEmployee employee, ArrayList<Timeoff> timeoffs){
        for(Timeoff to : timeoffs){
            if(to.getEmployeeId() == employee.getID()){
                return to.countTimeoffDays();
            }
        }
        return 0;
    }

    public String getData(){
        StringBuilder sb = new StringBuilder();
        sb.append(scheduleId).append(" ").append(hours).append(" ").append(shiftLength).append(" ").append(minimumRequiredStaff).append("\n");
        for(Shift shift : shifts){
            sb.append(shift.getEmployeeId()).append(" ").append(shift.getShiftDate()).append(" ").append(shift.getStartTime()).append(" ").append(shift.getEndTime()).append("\n");
        }
        sb.append("ENDSHIFTS\n");
        return sb.toString();
    }
}
