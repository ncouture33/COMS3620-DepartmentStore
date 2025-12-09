package StoreFloor;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * User interface for personal shopping appointment management.
 * Handles interactions for both sales associates and personal shopping coordinators.
 */
public class AppointmentUI {
    private AppointmentService appointmentService;
    private Scanner scanner;

    public AppointmentUI(Scanner scanner) {
        this.appointmentService = new AppointmentService();
        this.scanner = scanner;
    }

    /**
     * Main menu for appointment operations.
     */
    public void showAppointmentMenu() {
        while (true) {
            System.out.println("\n=== Personal Shopping Appointment System ===");
            System.out.println("1: Schedule a personal shopping appointment (Sales Associate)");
            System.out.println("2: Review appointment requests (Coordinator)");
            System.out.println("3: View scheduled appointments");
            System.out.println("4: Cancel an appointment (Sales Associate)");
            System.out.println("5: Back to Store Floor");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                scheduleAppointmentFlow();
            } else if (choice.equals("2")) {
                reviewAppointmentsFlow();
            } else if (choice.equals("3")) {
                viewScheduledAppointments();
            } else if (choice.equals("4")) {
                cancelAppointmentFlow();
            } else if (choice.equals("5") || choice.equalsIgnoreCase("back")) {
                break;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Flow for sales associate to schedule an appointment.
     */
    private void scheduleAppointmentFlow() {
        System.out.println("\n=== Schedule Personal Shopping Appointment ===");

        // Step 1: Display available shifts
        ArrayList<AppointmentService.ShiftSlot> availableShifts = appointmentService.getAvailableShifts();

        if (availableShifts.isEmpty()) {
            System.out.println("No available shifts at this time.");
            return;
        }

        System.out.println("\nAvailable shift slots:");
        for (AppointmentService.ShiftSlot shift : availableShifts) {
            System.out.println(shift);
        }

        // Step 2: Customer selects shift
        System.out.print("\nEnter the slot number you wish to book: ");
        int selectedSlot = 0;
        try {
            selectedSlot = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid slot number.");
            return;
        }

        AppointmentService.ShiftSlot selectedShift = null;
        for (AppointmentService.ShiftSlot shift : availableShifts) {
            if (shift.getSlotID() == selectedSlot) {
                selectedShift = shift;
                break;
            }
        }

        if (selectedShift == null) {
            System.out.println("Selected slot not found.");
            return;
        }

        System.out.println("Selected shift: " + selectedShift);

        // Step 3: Collect customer information
        System.out.print("Enter customer ID: ");
        int customerID = 0;
        try {
            customerID = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid customer ID.");
            return;
        }

        System.out.print("Enter customer name: ");
        String customerName = scanner.nextLine().trim();

        System.out.print("Enter customer email: ");
        String customerEmail = scanner.nextLine().trim();

        System.out.print("Enter appointment date (MM/DD/YYYY): ");
        String appointmentDate = scanner.nextLine().trim();

        System.out.print("Enter special requests (or press Enter for none): ");
        String specialRequests = scanner.nextLine().trim();

        // Step 4: Create appointment and remove shift
        int appointmentID = appointmentService.createAppointmentRequest(customerID, customerName, customerEmail,
                selectedShift.getEmployeeID(), appointmentDate, selectedShift.getTimeSlot(),
                selectedShift.getDepartment(), specialRequests);

        if (appointmentID > 0) {
            if (appointmentService.removeShiftFromAvailability(selectedShift.getEmployeeID(),
                    selectedShift.getTimeSlot(), selectedShift.getDepartment())) {
                System.out.println("Appointment request submitted successfully!");
                System.out.println("Appointment ID: " + appointmentID);
                System.out.println("Status: In Review");
            } else {
                System.out.println("Error: Appointment created but could not remove shift from availability.");
            }
        } else {
            System.out.println("Error creating appointment request.");
        }
    }

    /**
     * Flow for coordinator to review appointment requests.
     */
    private void reviewAppointmentsFlow() {
        System.out.println("\n=== Review Appointment Requests ===");

        ArrayList<PersonalShoppingAppointment> pendingAppointments = appointmentService.getPendingAppointments();

        if (pendingAppointments.isEmpty()) {
            System.out.println("No pending appointment requests.");
            return;
        }

        System.out.println("Pending appointment requests:");
        for (PersonalShoppingAppointment apt : pendingAppointments) {
            displayAppointmentDetails(apt);
        }

        System.out.print("\nEnter appointment ID to review (or press Enter to cancel): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        int appointmentID = 0;
        try {
            appointmentID = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid appointment ID.");
            return;
        }

        PersonalShoppingAppointment selectedAppointment = null;
        for (PersonalShoppingAppointment apt : pendingAppointments) {
            if (apt.getAppointmentID() == appointmentID) {
                selectedAppointment = apt;
                break;
            }
        }

        if (selectedAppointment == null) {
            System.out.println("Appointment not found.");
            return;
        }

        System.out.println("\n=== Review Details ===");
        displayAppointmentDetails(selectedAppointment);

        System.out.println("\nOptions:");
        System.out.println("1: Approve appointment");
        System.out.println("2: Request revision (Needs Revision status)");
        System.out.println("3: Decline appointment");
        System.out.println("4: Cancel review");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            if (appointmentService.approveAppointment(appointmentID)) {
                System.out.println("Appointment approved and scheduled!");
            } else {
                System.out.println("Error approving appointment.");
            }
        } else if (choice.equals("2")) {
            System.out.print("Enter revision comment: ");
            String comment = scanner.nextLine().trim();
            if (appointmentService.updateAppointmentStatus(appointmentID, "Needs Revision", comment)) {
                System.out.println("Appointment status updated to 'Needs Revision'.");
                System.out.println("Customer will be notified to revise their request.");
            } else {
                System.out.println("Error updating appointment status.");
            }
        } else if (choice.equals("3")) {
            if (appointmentService.declineAppointment(appointmentID)) {
                System.out.println("Appointment declined.");
            } else {
                System.out.println("Error declining appointment.");
            }
        } else if (choice.equals("4")) {
            System.out.println("Review cancelled.");
        } else {
            System.out.println("Invalid choice.");
        }
    }

    /**
     * Display appointment details.
     */
    private void displayAppointmentDetails(PersonalShoppingAppointment apt) {
        System.out.println("\n---");
        System.out.println("Appointment ID: " + apt.getAppointmentID());
        System.out.println("Customer: " + apt.getCustomerName() + " (ID: " + apt.getCustomerID() + ")");
        System.out.println("Email: " + apt.getCustomerEmail());
        System.out.println("Date: " + apt.getAppointmentDate());
        System.out.println("Time: " + apt.getAppointmentTime());
        System.out.println("Department: " + apt.getDepartment());
        System.out.println("Special Requests: "
                + (apt.getSpecialRequests().isEmpty() ? "None" : apt.getSpecialRequests()));
        System.out.println("Status: " + apt.getStatus());
        if (!apt.getCoordinatorComment().isEmpty()) {
            System.out.println("Coordinator Comment: " + apt.getCoordinatorComment());
        }
        System.out.println("---");
    }

    /**
     * View all scheduled appointments.
     */
    private void viewScheduledAppointments() {
        System.out.println("\n=== Scheduled Appointments ===");

        ArrayList<PersonalShoppingAppointment> scheduled = appointmentService.getScheduledAppointments();

        if (scheduled.isEmpty()) {
            System.out.println("No scheduled appointments.");
            return;
        }

        for (PersonalShoppingAppointment apt : scheduled) {
            displayAppointmentDetails(apt);
        }
    }

    /**
     * Flow for sales associate to cancel appointments.
     */
    private void cancelAppointmentFlow() {
        System.out.println("\n=== Cancel Personal Shopping Appointment (Sales Associate) ===");

        // Display both pending and scheduled appointments that can be cancelled
        ArrayList<PersonalShoppingAppointment> pendingAppointments = appointmentService.getPendingAppointments();
        ArrayList<PersonalShoppingAppointment> scheduledAppointments = appointmentService.getScheduledAppointments();

        if (pendingAppointments.isEmpty() && scheduledAppointments.isEmpty()) {
            System.out.println("No appointments available to cancel.");
            return;
        }

        System.out.println("\nPending Appointments:");
        for (PersonalShoppingAppointment apt : pendingAppointments) {
            displayAppointmentDetails(apt);
        }

        System.out.println("\nScheduled Appointments:");
        for (PersonalShoppingAppointment apt : scheduledAppointments) {
            displayAppointmentDetails(apt);
        }

        System.out.print("Enter appointment ID to cancel: ");
        int appointmentID = 0;
        try {
            appointmentID = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid appointment ID.");
            return;
        }

        PersonalShoppingAppointment toCancel = null;
        boolean isPending = false;

        // Check pending appointments
        for (PersonalShoppingAppointment apt : pendingAppointments) {
            if (apt.getAppointmentID() == appointmentID) {
                toCancel = apt;
                isPending = true;
                break;
            }
        }

        // Check scheduled appointments if not found in pending
        if (toCancel == null) {
            for (PersonalShoppingAppointment apt : scheduledAppointments) {
                if (apt.getAppointmentID() == appointmentID) {
                    toCancel = apt;
                    isPending = false;
                    break;
                }
            }
        }

        if (toCancel == null) {
            System.out.println("Appointment not found.");
            return;
        }

        System.out.println("\nAppointment to cancel:");
        displayAppointmentDetails(toCancel);

        System.out.print("Are you sure you want to cancel this appointment? (Y/N): ");
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("y")) {
            if (appointmentService.cancelAppointment(appointmentID)) {
                System.out.println("Appointment cancelled successfully.");
            } else {
                System.out.println("Error cancelling appointment.");
            }
        } else {
            System.out.println("Cancellation aborted.");
        }
    }
}
