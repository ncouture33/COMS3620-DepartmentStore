package StoreOperations;

public class ClockTime {
    protected int social;
    protected int date; // ex 120925
    protected String clockInTime; // format, HOURMIN -> ex: 12:20
    protected String clockOutTime = "0000"; // format, HOURMIN -> ex: 5:20

    public ClockTime(int social, int date, String clockInTime) {
        social = this.social;
        date = this.date;
        clockInTime = this.clockInTime;
    }

    // not a value which should need to be changed, no set
    public int getSocial() {
        return social;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        date = this.date;
    }

    public String getclockInTime() {
        return clockInTime;
    }

    public void setclockInTime(String clockInTime) {
        clockInTime = this.clockInTime;
    }

    public String getclockOutTime() {
        return clockOutTime;
    }

    public void setclockOutTime(String clockOutTime) {
        clockOutTime = this.clockOutTime;
    }

    // gets total hours worked in a shift
    public double getTotalHours() {
        // parse provided string hours and minutes
        int inHours = Integer.parseInt(clockInTime.substring(0, clockInTime.indexOf(":")));
        int inMinutes = Integer.parseInt(clockInTime.substring(clockInTime.indexOf(":") + 1));
        int outHours = Integer.parseInt(clockOutTime.substring(0, clockOutTime.indexOf(":")));
        int outMinutes = Integer.parseInt(clockOutTime.substring(clockOutTime.indexOf(":") + 1));

        // total minutes since start of day
        int totalInMinutes = inHours * 60 + inMinutes;
        int totalOutMinutes = outHours * 60 + outMinutes;

        // handle potential shift that passes midnight
        if (totalOutMinutes < totalInMinutes) {
            totalOutMinutes += 24 * 60; // add 24 hours
        }

        int minutesWorked = totalOutMinutes - totalInMinutes;

        // converting to hours as decimal
        double decimalHours = minutesWorked / 60.0;

        // rounding value to nearest tenth to avoid excessive decimals
        return Math.round(decimalHours * 10.0) / 10.0;
    }

    public String toString() {
        return "ID: " + social + ", ClockInTime: " + clockInTime + "ClockOutTime: " + clockOutTime + ", Date: " + date
                + "TotalTime: " + getTotalHours();
    }

    public String getData() {
        return social + " " + date + " " + clockInTime + " ";
    }

}
