package StoreFloor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Service class for managing personal shopping appointments.
 * Handles file I/O operations, appointment creation, and status management.
 */
public class AppointmentService {
    private static final String SHIFT_AVAILABILITY_FILE = "shift_availability.txt";
    private static final String PENDING_APPOINTMENTS_FILE = "pending_appointments.txt";
    private static final String SCHEDULED_APPOINTMENTS_FILE = "scheduled_appointments.txt";

    /**
     * Get all available shifts from the shift availability file.
     */
    public ArrayList<ShiftSlot> getAvailableShifts() {
        ArrayList<ShiftSlot> shifts = new ArrayList<>();
        File file = new File(SHIFT_AVAILABILITY_FILE);

        if (!file.exists()) {
            System.out.println("Shift availability file not found.");
            return shifts;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int slotID = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    int employeeID = Integer.parseInt(parts[0]);
                    String timeSlot = parts[1];
                    String department = parts[2];
                    shifts.add(new ShiftSlot(slotID++, employeeID, timeSlot, department));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading shift availability file: " + e.getMessage());
        }

        return shifts;
    }

    /**
     * Create a new appointment request and save it to pending appointments file.
     */
    public int createAppointmentRequest(int customerID, String customerName, String customerEmail, int employeeID,
            String appointmentDate, String appointmentTime, String department, String specialRequests) {
        // Generate appointment ID
        int appointmentID = generateAppointmentID();

        PersonalShoppingAppointment appointment = new PersonalShoppingAppointment(appointmentID, customerID,
                customerName, customerEmail, employeeID, appointmentDate, appointmentTime, department,
                specialRequests);

        // Write to pending appointments file
        try (FileWriter writer = new FileWriter(PENDING_APPOINTMENTS_FILE, true)) {
            writer.write(appointment.getData() + "\n");
            System.out.println("Appointment request created with ID: " + appointmentID);
        } catch (IOException e) {
            System.out.println("Error writing appointment request: " + e.getMessage());
            return -1;
        }

        return appointmentID;
    }

    /**
     * Get all pending appointment requests for coordinator review.
     */
    public ArrayList<PersonalShoppingAppointment> getPendingAppointments() {
        ArrayList<PersonalShoppingAppointment> appointments = new ArrayList<>();
        File file = new File(PENDING_APPOINTMENTS_FILE);

        if (!file.exists()) {
            return appointments;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                PersonalShoppingAppointment apt = PersonalShoppingAppointment.parseAppointment(line);
                if (apt != null) {
                    appointments.add(apt);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading pending appointments: " + e.getMessage());
        }

        return appointments;
    }

    /**
     * Get all scheduled appointments.
     */
    public ArrayList<PersonalShoppingAppointment> getScheduledAppointments() {
        ArrayList<PersonalShoppingAppointment> appointments = new ArrayList<>();
        File file = new File(SCHEDULED_APPOINTMENTS_FILE);

        if (!file.exists()) {
            return appointments;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                PersonalShoppingAppointment apt = PersonalShoppingAppointment.parseAppointment(line);
                if (apt != null) {
                    appointments.add(apt);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading scheduled appointments: " + e.getMessage());
        }

        return appointments;
    }

    /**
     * Update an appointment's status and coordinator comment.
     */
    public boolean updateAppointmentStatus(int appointmentID, String newStatus, String coordinatorComment) {
        ArrayList<PersonalShoppingAppointment> appointments = getPendingAppointments();
        boolean found = false;

        try (FileWriter writer = new FileWriter(PENDING_APPOINTMENTS_FILE, false)) {
            for (PersonalShoppingAppointment apt : appointments) {
                if (apt.getAppointmentID() == appointmentID) {
                    apt.setStatus(newStatus);
                    apt.setCoordinatorComment(coordinatorComment);
                    found = true;
                }
                writer.write(apt.getData() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error updating appointment: " + e.getMessage());
            return false;
        }

        return found;
    }

    /**
     * Approve an appointment and move it from pending to scheduled.
     */
    public boolean approveAppointment(int appointmentID) {
        ArrayList<PersonalShoppingAppointment> pendingAppointments = getPendingAppointments();
        PersonalShoppingAppointment approved = null;

        // Find and remove from pending
        try (FileWriter writer = new FileWriter(PENDING_APPOINTMENTS_FILE, false)) {
            for (PersonalShoppingAppointment apt : pendingAppointments) {
                if (apt.getAppointmentID() == appointmentID) {
                    apt.setStatus("Scheduled");
                    approved = apt;
                } else {
                    writer.write(apt.getData() + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating pending appointments: " + e.getMessage());
            return false;
        }

        if (approved != null) {
            // Add to scheduled appointments
            try (FileWriter writer = new FileWriter(SCHEDULED_APPOINTMENTS_FILE, true)) {
                writer.write(approved.getData() + "\n");
                System.out.println("Appointment approved and scheduled.");
                return true;
            } catch (IOException e) {
                System.out.println("Error adding to scheduled appointments: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Cancel an appointment and restore the shift to availability.
     * Can cancel from either pending or scheduled appointments.
     */
    public boolean cancelAppointment(int appointmentID) {
        ArrayList<PersonalShoppingAppointment> pendingAppointments = getPendingAppointments();
        ArrayList<PersonalShoppingAppointment> scheduledAppointments = getScheduledAppointments();
        PersonalShoppingAppointment toCancel = null;
        boolean fromPending = false;

        // Find appointment in pending
        for (PersonalShoppingAppointment apt : pendingAppointments) {
            if (apt.getAppointmentID() == appointmentID) {
                toCancel = apt;
                fromPending = true;
                break;
            }
        }

        // Find appointment in scheduled if not found in pending
        if (toCancel == null) {
            for (PersonalShoppingAppointment apt : scheduledAppointments) {
                if (apt.getAppointmentID() == appointmentID) {
                    toCancel = apt;
                    fromPending = false;
                    break;
                }
            }
        }

        if (toCancel == null) {
            System.out.println("Appointment not found.");
            return false;
        }

        // Remove from appropriate file
        if (fromPending) {
            try (FileWriter writer = new FileWriter(PENDING_APPOINTMENTS_FILE, false)) {
                for (PersonalShoppingAppointment apt : pendingAppointments) {
                    if (apt.getAppointmentID() != appointmentID) {
                        writer.write(apt.getData() + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error removing cancelled appointment: " + e.getMessage());
                return false;
            }
        } else {
            try (FileWriter writer = new FileWriter(SCHEDULED_APPOINTMENTS_FILE, false)) {
                for (PersonalShoppingAppointment apt : scheduledAppointments) {
                    if (apt.getAppointmentID() != appointmentID) {
                        writer.write(apt.getData() + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error removing cancelled appointment: " + e.getMessage());
                return false;
            }
        }

        // Restore shift to availability
        restoreShiftAvailability(toCancel.getEmployeeID(), toCancel.getAppointmentTime(), toCancel.getDepartment());

        System.out.println("Appointment cancelled and shift restored to availability.");
        return true;
    }

    /**
     * Decline an appointment and restore the shift to availability.
     */
    public boolean declineAppointment(int appointmentID) {
        ArrayList<PersonalShoppingAppointment> appointments = getPendingAppointments();
        PersonalShoppingAppointment toDecline = null;

        // Find appointment
        for (PersonalShoppingAppointment apt : appointments) {
            if (apt.getAppointmentID() == appointmentID) {
                toDecline = apt;
                break;
            }
        }

        if (toDecline == null) {
            System.out.println("Appointment not found in pending appointments.");
            return false;
        }

        // Remove from pending
        try (FileWriter writer = new FileWriter(PENDING_APPOINTMENTS_FILE, false)) {
            for (PersonalShoppingAppointment apt : appointments) {
                if (apt.getAppointmentID() != appointmentID) {
                    writer.write(apt.getData() + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error removing declined appointment: " + e.getMessage());
            return false;
        }

        // Restore shift to availability
        restoreShiftAvailability(toDecline.getEmployeeID(), toDecline.getAppointmentTime(), toDecline.getDepartment());

        System.out.println("Appointment declined and shift restored to availability.");
        return true;
    }

    /**
     * Restore a shift slot to the availability file.
     */
    private void restoreShiftAvailability(int employeeID, String timeSlot, String department) {
        try (FileWriter writer = new FileWriter(SHIFT_AVAILABILITY_FILE, true)) {
            writer.write(employeeID + "|" + timeSlot + "|" + department + "\n");
            System.out.println("Shift slot restored to availability.");
        } catch (IOException e) {
            System.out.println("Error restoring shift availability: " + e.getMessage());
        }
    }

    /**
     * Remove a shift from the availability file.
     */
    public boolean removeShiftFromAvailability(int employeeID, String timeSlot, String department) {
        ArrayList<String> shifts = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(SHIFT_AVAILABILITY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 3 && Integer.parseInt(parts[0]) == employeeID && parts[1].equals(timeSlot)
                        && parts[2].equals(department)) {
                    found = true;
                } else {
                    shifts.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading shift availability file: " + e.getMessage());
            return false;
        }

        if (!found) {
            System.out.println("Shift not found in availability file.");
            return false;
        }

        try (FileWriter writer = new FileWriter(SHIFT_AVAILABILITY_FILE, false)) {
            for (String shift : shifts) {
                writer.write(shift + "\n");
            }
            System.out.println("Shift removed from availability.");
            return true;
        } catch (IOException e) {
            System.out.println("Error updating shift availability file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate a unique appointment ID based on pending and scheduled appointments.
     */
    private int generateAppointmentID() {
        int maxID = 0;
        ArrayList<PersonalShoppingAppointment> pending = getPendingAppointments();
        ArrayList<PersonalShoppingAppointment> scheduled = getScheduledAppointments();

        for (PersonalShoppingAppointment apt : pending) {
            maxID = Math.max(maxID, apt.getAppointmentID());
        }

        for (PersonalShoppingAppointment apt : scheduled) {
            maxID = Math.max(maxID, apt.getAppointmentID());
        }

        return maxID + 1;
    }

    /**
     * Inner class representing a shift slot.
     */
    public static class ShiftSlot {
        private int slotID;
        private int employeeID;
        private String timeSlot;
        private String department;

        public ShiftSlot(int slotID, int employeeID, String timeSlot, String department) {
            this.slotID = slotID;
            this.employeeID = employeeID;
            this.timeSlot = timeSlot;
            this.department = department;
        }

        public int getSlotID() {
            return slotID;
        }

        public int getEmployeeID() {
            return employeeID;
        }

        public String getTimeSlot() {
            return timeSlot;
        }

        public String getDepartment() {
            return department;
        }

        @Override
        public String toString() {
            return "Slot " + slotID + " - Employee ID: " + employeeID + " | Time: " + timeSlot + " | Department: "
                    + department;
        }
    }
}
