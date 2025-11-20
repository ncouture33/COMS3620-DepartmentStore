package StoreOperations;

public class ClockTime {
    protected int social;
    protected int date; // ex 120925
    protected String clockInTime; // format: HH:MM
    protected String clockOutTime = "00:00"; // format: HH:MM

    public ClockTime(int social, int date, String clockInTime) {
        this.social = social;
        this.date = date;
        this.clockInTime = clockInTime;
    }

    public int getSocial() {
        return social;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(String clockInTime) {
        this.clockInTime = clockInTime;
    }

    public String getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(String clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    // gets total hours worked in a shift
    public double getTotalHours() {
        // Parse hours and minutes from HH:mm format
        int inHours = Integer.parseInt(clockInTime.substring(0, clockInTime.indexOf(":")));
        int inMinutes = Integer.parseInt(clockInTime.substring(clockInTime.indexOf(":") + 1));
        int outHours = Integer.parseInt(clockOutTime.substring(0, clockOutTime.indexOf(":")));
        int outMinutes = Integer.parseInt(clockOutTime.substring(clockOutTime.indexOf(":") + 1));

        // convert both times to total minutes from start of day
        int totalInMinutes = inHours * 60 + inMinutes;
        int totalOutMinutes = outHours * 60 + outMinutes;

        // handle overnight shift (example: 23:00 → 02:00)
        if (totalOutMinutes < totalInMinutes) {
            totalOutMinutes += 24 * 60;
        }

        int minutesWorked = totalOutMinutes - totalInMinutes;

        // convert to hours (decimal format) and round to nearest 0.1
        double decimalHours = minutesWorked / 60.0;
        return Math.round(decimalHours * 10.0) / 10.0;
    }

    @Override
    public String toString() {
        return "ID: " + social +
                ", ClockInTime: " + clockInTime +
                ", ClockOutTime: " + clockOutTime +
                ", Date: " + date +
                ", TotalTime: " + getTotalHours();
    }

    public String getData() {
        return social + " " + date + " " + getTotalHours() + " ";
    }
}
