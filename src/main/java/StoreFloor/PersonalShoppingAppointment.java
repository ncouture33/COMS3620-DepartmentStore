package StoreFloor;

import Utils.Data;

/**
 * Represents a personal shopping appointment request in the system.
 * Tracks appointment details, status, and coordinator feedback.
 */
public class PersonalShoppingAppointment implements Data {
    private int appointmentID;
    private int employeeID;
    private String appointmentDate;
    private String appointmentTime;
    private String department;
    private String specialRequests;
    private String status; // "In Review", "Needs Revision", "Approved", "Scheduled", "Cancelled"
    private String coordinatorComment;
    private int customerID;
    private String customerName;
    private String customerEmail;

    public PersonalShoppingAppointment(int appointmentID, int customerID, String customerName, String customerEmail,
            int employeeID, String appointmentDate, String appointmentTime, String department, String specialRequests) {
        this.appointmentID = appointmentID;
        this.customerID = customerID;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.employeeID = employeeID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.department = department;
        this.specialRequests = specialRequests;
        this.status = "In Review";
        this.coordinatorComment = "";
    }

    // Getters
    public int getAppointmentID() {
        return appointmentID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public String getDepartment() {
        return department;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public String getStatus() {
        return status;
    }

    public String getCoordinatorComment() {
        return coordinatorComment;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void setCoordinatorComment(String comment) {
        this.coordinatorComment = comment;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    /**
     * Returns the appointment data in pipe-delimited format for file storage.
     * Format: appointmentID|customerID|customerName|customerEmail|employeeID|date|time|department|specialRequests|status|coordinatorComment
     */
    @Override
    public String getData() {
        return appointmentID + "|" + customerID + "|" + customerName + "|" + customerEmail + "|" + employeeID + "|"
                + appointmentDate + "|" + appointmentTime + "|" + department + "|" + specialRequests + "|" + status
                + "|" + coordinatorComment;
    }

    /**
     * Parse appointment data from pipe-delimited string.
     */
    public static PersonalShoppingAppointment parseAppointment(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 10)
            return null;

        try {
            int appointmentID = Integer.parseInt(parts[0]);
            int customerID = Integer.parseInt(parts[1]);
            String customerName = parts[2];
            String customerEmail = parts[3];
            int employeeID = Integer.parseInt(parts[4]);
            String date = parts[5];
            String time = parts[6];
            String department = parts[7];
            String specialRequests = parts[8];
            String status = parts[9];
            String coordinatorComment = parts.length > 10 ? parts[10] : "";

            PersonalShoppingAppointment apt = new PersonalShoppingAppointment(appointmentID, customerID, customerName,
                    customerEmail, employeeID, date, time, department, specialRequests);
            apt.setStatus(status);
            apt.setCoordinatorComment(coordinatorComment);

            return apt;
        } catch (Exception e) {
            return null;
        }
    }
}
