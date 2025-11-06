package HR;

import Utils.Database;
import Utils.DatabaseWriter;

import java.util.ArrayList;
import java.util.Scanner;

public class Util {

    public static void runHR(){
        
        DatabaseWriter database = new Database();
        System.out.println("HR console running...");
        Scanner scanner = new Scanner(System.in);

        while (true){

            System.out.print(
                "1: Onboard an employee\n" +
                "2: Offboard an employee\n" +
                "3: View all employees\n" +
                "4: View Upcoming Payroll\n" +
                "5: Execute Payroll\n" +
                "6: Modify a timecard\n" +
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

                    Salary emp = new Salary(BaseEmployee.getNextEmployeeID(), fName, lName, DOB, social, salary);

                    emp.setAccount(account);

                    database.writeEmployee(emp);

                } else if (empType.equals("H")) {
                    System.out.println("Enter employee hourly rate: ");
                    double hourlyRate = Double.parseDouble(scanner.nextLine());
                    System.out.println("Enter employee overtime rate: ");
                    double overtimeRate = Double.parseDouble(scanner.nextLine());

                    Hourly emp = new Hourly(BaseEmployee.getNextEmployeeID(), fName, lName, DOB, social, hourlyRate, overtimeRate);

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
                // employee has been offboarded and now should be written to priorEmployee and removed from offboard
                database.removeFromEmployee(empID);
                if(hasOffboarded == 1){
                    System.out.println("Enter 1 if employee returned company property, otherwise enter 0");
                    int propertyReturned = scanner.nextInt();
                    if(propertyReturned == 1){
                        database.writePriorEmployee(database.getOffboardingEmployee(empID), "Property has been returned");
                    }
                    else{
                        database.writePriorEmployee(database.getOffboardingEmployee(empID), "Property has not been returned");
                    }
                }
                database.removeOffboardingEmployee(empID);
            }
            else if (command.equals("3")){
                //todo
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

            //More options go here

            if (command.equals("exit")){
                System.out.println("Exiting HR System.");
                break;
            }
        }

        scanner.close();
    }

}
