package StoreOperations;

import java.util.ArrayList;
import java.util.Scanner;

import HR.BaseEmployee;
import HR.TimeCard;
import StoreFloor.StorePOS;
import Utils.Database;
import Utils.DatabaseWriter;
// no direct POS item/payment handling here anymore; StoreFloor will use the active POS

/**
 * Simple CLI for store operations: register new employee or login to a register
 * (POS).
 * Uses existing project classes and a minimal interactive flow.
 */
public class Util {
    public static void runOperations(Scanner scanner) {
        DatabaseWriter database = new Database();
        while (true) {
            System.out.println("\nStore Operations - choose an option:");
            System.out.println("1: Register new cashier");
            StorePOS current = Session.getCurrentPOS();
            boolean loggedIn = (current != null && current.getLoggedInEmployee() != null);
            if (loggedIn) {
                System.out.println("2: Logout current register");
            } else {
                System.out.println("2: Login to register");
            }
            System.out.println("3: Clock In");
            System.out.println("4: Clock Out");
            System.out.println("5: Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();
            if (choice.equals("1")) {
                registerNewCashier(scanner);
            } else if (choice.equals("2")) {
                // perform login or logout depending on current session state
                StorePOS cur = Session.getCurrentPOS();
                boolean isLogged = (cur != null && cur.getLoggedInEmployee() != null);
                if (isLogged) {
                    logoutOfRegister(scanner);
                } else {
                    loginToRegister(scanner);
                }
            } else if (choice.equals("3")) { // clocking in an employee
                boolean found = false;
                System.out.println("Enter ClockIn ID: ");
                int social = scanner.nextInt();
                scanner.nextLine();
                // need to check they're an existing employee
                ArrayList<BaseEmployee> employees = database.getEmployees();
                // find if employee exists
                for (int i = 0; i < employees.size(); i++) {
                    if (employees.get(i).getSocial() == social) { // employee exists
                        found = true;
                        BaseEmployee emp = employees.get(i);
                        System.out.println("Enter Date: (ex: 112025)");
                        int date = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Enter Time (23:00): ");
                        String time = scanner.nextLine();
                        database.writeClockedInEmployee(emp, time, date); // employee has clocked in
                        break;
                    }
                }
                if (!found) {
                    System.out.println("Employee not found");
                } else {
                    System.out.println("You have successfully clocked in"); // need to add check that they're
                                                                            // not,already clocked in
                }
            } else if (choice.equals("4")) {
                boolean found = false;
                ClockTime myemp;
                System.out.println("Enter Clockout ID: ");
                int social = scanner.nextInt();
                scanner.nextLine();
                // need to get the specific employee
                ArrayList<BaseEmployee> employees = database.getEmployees();
                // need to check they're logged in
                ArrayList<ClockTime> clockedInEmployees = database.getClockedEmployees();
                // find if employee exists
                for (int i = 0; i < clockedInEmployees.size(); i++) {
                    System.out.println(clockedInEmployees.get(i).getSocial());
                    if (clockedInEmployees.get(i).getSocial() == social) {
                        found = true;
                        myemp = clockedInEmployees.get(i);
                        System.out.println("Enter Time (ex 23:00): ");
                        String time = scanner.nextLine();
                        myemp.setClockOutTime(time);

                        for (int j = 0; j < employees.size(); j++) {
                            if (employees.get(j).getSocial() == social) {
                                BaseEmployee emp = employees.get(j);
                                TimeCard timecard = emp.getTimecard();
                                double timeWorked = myemp.getTotalHours();
                                timecard.increaseHoursWorked(timeWorked);
                                break;
                            }
                        }

                        if (database.clockOutEmployee(myemp)) {
                            System.out.println("You have been clocked out");
                        }
                        database.writeTimeHistory(myemp);
                        break;
                    }
                }

                if (!found) {
                    System.out.println("Employee not found");
                } else {
                    System.out.println("Your Time History has successfully been updated"); // need to add check that
                                                                                           // they're not,
                    // already clocked in
                }
            } else if (choice.equals("5") || choice.equalsIgnoreCase("back")) {
                break;
            } else {
                System.out.println("Unknown choice");
            }
        }
    }

	private static void logoutOfRegister(Scanner scanner){
		StorePOS pos = Session.getCurrentPOS();
		if(pos == null || pos.getLoggedInEmployee() == null){
			System.out.println("No active logged-in register to log out from.");
			return;
		}
		HR.BaseEmployee employee = pos.getLoggedInEmployee();
		System.out.print("Enter PIN to confirm logout: ");
		String pin = scanner.nextLine().trim();
		if (!employee.verifyPin(pin)) {
			System.out.println("Invalid PIN. Logout aborted.");
			return;
		}

		String name = employee.getFName() + " " + employee.getLName();
		pos.logoutEmployee();
		// clear the session
		Session.setCurrentPOS(null);
		System.out.println("Logged out employee from active register: " + name);
	}

    // private static void logoutOfRegister(Scanner scanner) {
    //     StorePOS pos = Session.getCurrentPOS();
    //     if (pos == null || pos.getLoggedInEmployee() == null) {
    //         System.out.println("No active logged-in register to log out from.");
    //         return;
    //     }
    //     String name = pos.getLoggedInEmployee().getFName() + " " + pos.getLoggedInEmployee().getLName();
    //     pos.logoutEmployee();
    //     // clear the session
    //     Session.setCurrentPOS(null);
    //     System.out.println("Logged out employee from active register: " + name);
    // }

    private static void registerNewCashier(Scanner scanner) {
        try {
            System.out.print("Enter existing employee ID to grant register access: ");
            String idStr = scanner.nextLine().trim();
            int id = Integer.parseInt(idStr);

            Database db = new Database();
            java.util.ArrayList<HR.BaseEmployee> employees = db.getEmployees();
            HR.BaseEmployee match = null;
            for (HR.BaseEmployee e : employees) {
                if (e.getID() == id) {
                    match = e;
                    break;
                }
            }
            if (match == null) {
                System.out.println(
                        "No employee with ID " + id + " found in employees.txt. Cannot grant register access.");
                return;
            }

            System.out.println("Found employee: " + match.getFName() + " " + match.getLName());
            if (match.getUsername() != null && !match.getUsername().isEmpty()) {
                System.out.print(
                        "This employee already has a username (" + match.getUsername() + "). Overwrite? (Y/N): ");
                String yn = scanner.nextLine().trim();
                if (!yn.equalsIgnoreCase("y")) {
                    System.out.println("Aborting registration.");
                    return;
                }
            }

            System.out.print("Choose username: ");
            String username = scanner.nextLine().trim();
            match.setUsername(username);
            System.out.print("Choose password: ");
            String password = scanner.nextLine();
            match.setPassword(password);
            System.out.print("Choose numeric PIN: ");
            String pin = scanner.nextLine().trim();
            match.setPin(pin);

            // persist credentials only to registerEmployees.txt (do not modify
            // employees.txt)
            db.writeRegisterCredentials(match);
            System.out.println("Granted register access to employee ID: " + match.getID()
                    + ". Credentials saved to registerEmployees.txt");
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid ID entered.");
        } catch (Exception e) {
            System.out.println("Failed to register cashier: " + e.getMessage());
        }
    }

    private static void loginToRegister(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("PIN: ");
        String pin = scanner.nextLine().trim();

        // Prevent overwriting an already-active logged-in employee on the shared
        // session
        StorePOS current = Session.getCurrentPOS();
        if (current != null && current.getLoggedInEmployee() != null) {
            HR.BaseEmployee active = current.getLoggedInEmployee();
            System.out.println("A user is already logged in on the active register: " + active.getFName() + " "
                    + active.getLName() + ". Please logout first.");
            return;
        }

        // Use existing POS in session if present (and not logged in), otherwise create
        // a new POS
        StorePOS pos = (current != null) ? current : new StorePOS();
        boolean ok = pos.loginEmployee(username, password, pin);
        if (!ok) {
            System.out.println("Login failed.");
            return;
        }
        // Store POS in the shared session so StoreFloor actions can use it
        Session.setCurrentPOS(pos);
        System.out.println("Login successful. POS is now active (go to Store Floor Actions to use the register).");
    }
}