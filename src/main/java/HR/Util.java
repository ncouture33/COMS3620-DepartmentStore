package HR;

import java.util.ArrayList;
import java.util.Scanner;

import HR.Orientation.OrientationTask;
import Utils.Database;
import Utils.DatabaseWriter;

public class Util {

    public static void runHR(Scanner passedScanner){
        
        DatabaseWriter database = new Database();
        System.out.println("HR console running...");
        Scanner scanner = passedScanner;

        while (true){

            System.out.print(
                "1: Onboard an employee\n" +
                "2: Offboard an employee\n" +
                "3: Generate a new schedule\n" +
                "4: View Upcoming Payroll\n" +
                "5: Execute Payroll\n" +
                "6: Orientation\n" +
                "7; Promotion/ Demotion\n"+
                "Type 'exit' to return to main menu.\n"
            );

            String command = scanner.nextLine();

            // Onboarding an employee
            if (command.equals("1")){
                
                // Get basic employee info
                System.out.println("Enter employee first name: ");
                String fName = scanner.nextLine();
                System.out.println("Enter employee last name: ");
                String lName = scanner.nextLine();
                System.out.println("Enter employee DOB (YYYYMMDD): ");
                int DOB = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter employee Social Security Number (XXXXXXXXX): ");
                int social = Integer.parseInt(scanner.nextLine());

                // Get employee type
                System.out.println("Is the employee salaried or hourly? (S/H):");
                String empType = scanner.nextLine();

                // Get account info
                System.out.println("Enter bank name for direct deposit:");
                String bankName = scanner.nextLine();
                System.out.println("Enter routing number: ");
                int routingNum = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter account number: ");
                int accountNum = Integer.parseInt(scanner.nextLine());

                // Create employee bank account
                Account account = new Account(bankName, routingNum, accountNum);
                // Create employee based on type
                if (empType.equals("S")) {
                    System.out.println("Enter employee salary: ");
                    int salary = Integer.parseInt(scanner.nextLine());
                    System.out.println("Enter Department: ");
                    String department = scanner.nextLine();
                    System.out.println("Enter Role: ");
                    String role = scanner.nextLine();

                    Salary emp = new Salary(BaseEmployee.getNextEmployeeID(), fName, lName, DOB, social, salary, department, role);

                    emp.setAccount(account);

                    database.writeEmployee(emp);

                } else if (empType.equals("H")) {
                    System.out.println("Enter employee hourly rate: ");
                    double hourlyRate = Double.parseDouble(scanner.nextLine());
                    System.out.println("Enter employee overtime rate: ");
                    double overtimeRate = Double.parseDouble(scanner.nextLine());
                    System.out.println("Enter Department: ");
                    String department = scanner.nextLine();
                    System.out.println("Enter Role: ");
                    String role = scanner.nextLine();

                    Hourly emp = new Hourly(BaseEmployee.getNextEmployeeID(), fName, lName, DOB, social, hourlyRate, overtimeRate, department, role);

                    emp.setAccount(account);

                    database.writeEmployee(emp);

                }

            }
            else if (command.equals("2")){
                // Get employee departure info
                System.out.println("Enter employeeID: ");
                int empID = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Enter Employee's Departure Date: ");
                String depDate = scanner.nextLine();
                System.out.println("Enter Employee's Reason for Departure: ");
                String reasonOfLeaving = scanner.nextLine();
                database.indicateEmployeeOffboarding(empID, depDate, reasonOfLeaving);
                System.out.println("Enter 1 if employee has completed onboarding tasks, otherwise enter 0:");
                int hasOffboarded = scanner.nextInt();
                //remove from employee
                // employee has been offboarded and now should be written to priorEmployee 
                database.removeFromEmployee(empID);
                if(hasOffboarded == 1){
                    //removed from offboarding bc its completed its tasks
                    System.out.println("Enter 1 if employee returned company property, otherwise enter 0");
                    int propertyReturned = scanner.nextInt();
                    if(propertyReturned == 1){
                        database.writePriorEmployee(database.getOffboardingEmployee(empID), "Property has been returned");
                    }
                    else{
                        database.writePriorEmployee(database.getOffboardingEmployee(empID), "Property has not been returned");
                    }
                    database.removeOffboardingEmployee(empID);

                }
            }
            else if (command.equals("3")){
                //schedule employee shifts
                System.out.println("Enter the Id's of employees you would like to exclude from scheduling, separated by commas (enter if none should be excluded):");
                //logic for excluding employees from scheduling to be implemented
                ArrayList<Integer> excludeList = new ArrayList<>();
                
                String excludeInput = scanner.nextLine();
                if (!excludeInput.isEmpty()){
                    String[] excludeIds = excludeInput.split(",");
                    for(String idStr : excludeIds){
                        try{
                            int id = Integer.parseInt(idStr.trim());
                            excludeList.add(id);
                        }catch(NumberFormatException e){
                            System.out.println("Invalid employee ID: " + idStr);
                        }
                    }
                }
                
                ArrayList<BaseEmployee> employees = database.getAllEmployeesExcluding(excludeList);
                System.out.println("Enter the number of hours that the store is open each day");
                int hoursOpen = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter the length of each shift in hours");
                int shiftLength = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter the minimum required staff per shift");
                int minStaff = Integer.parseInt(scanner.nextLine());

                int newShiftId = database.getNextScheduleID();
                Schedule schedule = new Schedule(newShiftId, hoursOpen, shiftLength, minStaff);
                schedule.determineSchedule(employees, database.getTimeoffs());
                
                System.out.println("Generated Schedule:");
                System.out.println(schedule.getData());

                System.out.println("Enter yes to confirm this schedule, or no to discard it:");
                String confirm = scanner.nextLine();
                
                if(confirm.equalsIgnoreCase("yes")){
                    database.writeSchedule(schedule);
                    System.out.println("Schedule saved.");
                }
                else{
                    System.out.println("Schedule discarded.");
                }
                
            }

            else if (command.equals("4")){
                Payroll pr = database.getPayroll();
                System.out.println(pr.toString());
            }

            else if (command.equals("5")){
                Payroll pr = database.getPayroll();
                System.out.println("Enter today's date");
                String date = scanner.nextLine();
                ArrayList<Paystub> list = pr.payEmployees(date);
                database.writePaystubs(list, date);
            }
            else if (command.equals("6")){
                // Orientation sub-menu
                while (true) {
                    System.out.println("Orientation Menu:\n1: Assign task to employee\n2: List tasks for employee\n3: Mark task completed\n4: Back");
                    String opt = scanner.nextLine();
                    if (opt.equals("1")) {
                        System.out.println("Enter employee ID:");
                        String idInput = scanner.nextLine();
                        int empId;
                        try { empId = Integer.parseInt(idInput.trim()); } catch (NumberFormatException e) { System.out.println("Invalid ID"); continue; }
                        System.out.println("Enter task name:");
                        String tName = scanner.nextLine();
                        System.out.println("Enter task description:");
                        String tDesc = scanner.nextLine();
                        database.addOrientationTask(empId, tName, tDesc);
                        System.out.println("Task added for employee " + empId);

                    } else if (opt.equals("2")) {
                        System.out.println("Enter employee ID:");
                        String idInput = scanner.nextLine();
                        int empId;
                        try { empId = Integer.parseInt(idInput.trim()); } catch (NumberFormatException e) { System.out.println("Invalid ID"); continue; }
                        ArrayList<OrientationTask> tasks = database.getOrientationTasks(empId);
                        if (tasks.isEmpty()) {
                            System.out.println("No tasks found for employee " + empId);
                        } else {
                            System.out.println("Tasks for employee " + empId + ":");
                            int i = 1;
                            for (OrientationTask t : tasks) {
                                System.out.println(i++ + ". [" + (t.isCompleted() ? "X" : " ") + "] " + t.getTaskName() + " - " + t.getTaskDescription());
                            }
                        }
                        
                    } else if (opt.equals("3")) {
                        System.out.println("Enter employee ID:");
                        String idInput = scanner.nextLine();
                        int empId;
                        try { empId = Integer.parseInt(idInput.trim()); } catch (NumberFormatException e) { System.out.println("Invalid ID"); continue; }
                        System.out.println("Enter task name to mark completed:");
                        String tName = scanner.nextLine();
                        boolean ok = database.completeOrientationTask(empId, tName);
                        if (ok) System.out.println("Marked completed."); else System.out.println("Task not found or already completed.");
                    } else if (opt.equals("4")) {
                        break;
                    } else {
                        System.out.println("Unknown option.");
                    }
                }
            }
            //More options go here

            else if (command.equals("exit")){
                System.out.println("Exiting HR System.");
                break;
            }
        }
    }
}