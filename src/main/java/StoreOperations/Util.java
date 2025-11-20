package StoreOperations;

import Utils.Database;
import Utils.DatabaseWriter;

import java.util.ArrayList;
import java.util.Scanner;

import HR.BaseEmployee;
import HR.TimeCard;

public class Util {

    public static void runStoreOperations(Scanner passedScanner) {
        DatabaseWriter database = new Database();
        System.out.println("Store Operations console running...");
        Scanner scanner = passedScanner;

        while (true) {

            System.out.print(
                    "1: Clock Out\n" +
                            "2: Clock Out\n" +
                            "Type 'exit' to return to main menu.\n");

            String command = scanner.nextLine();

            // Clocking in an Employee
            if (command.equals("1")) {
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
                        System.out.println("Enter Date: ");
                        int date = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Enter Time: ");
                        String time = scanner.nextLine();
                        database.writeClockedInEmployee(emp, time, date); // employee has clocked in
                        break;
                    }
                }
                if (!found) {
                    System.out.println("Employee not found");
                } else {
                    System.out.println("You have successfully clocked in"); // need to add check that they're not,
                                                                            // already clocked in
                }

            }
            // Clocking out an employee and updating this total time
            else if (command.equals("2")) {
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
                    if (clockedInEmployees.get(i).getSocial() == social) { // employee exists
                        found = true;
                        myemp = clockedInEmployees.get(i);
                        System.out.println("Enter Time: ");
                        String time = scanner.nextLine();
                        myemp.setclockOutTime(time);
                        // find if employee exists
                        for (int j = 0; i < employees.size(); j++) {
                            if (employees.get(j).getSocial() == social) { // employee exists
                                BaseEmployee emp = employees.get(j);
                                TimeCard timecard = emp.getTimecard();
                                double timeWorked = myemp.getTotalHours();
                                timecard.increaseHoursWorked(timeWorked); // updated employees time card
                                break;
                            }
                        }
                        database.clockOutEmployee(myemp); // employee has clocked out
                        break;
                    }
                }
                if (!found) {
                    System.out.println("Employee not found");
                } else {
                    System.out.println("You have successfully clocked out"); // need to add check that they're not,
                                                                             // already clocked in
                }
            }
        }
    }
}
