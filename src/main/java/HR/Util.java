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

            System.out.print("1: Onboard an employee\n" +
                    "2: Offboard an employee\n" +
                    "3: View all employees\n" +
                    "4: View Upcoming Payroll\n" +
                    "5: Execute Payroll\n" +
                    "6: Modify a timecard\n" +
                    "Type 'exit' to return to main menu.\n");


            String command = scanner.nextLine();
            if (command.equals("1")){
                //todo
                continue;
            }
            else if (command.equals("2")){
                //todo
                continue;
            }
            else if (command.equals("3")){
                //todo
                continue;
            }

            else if (command.equals("4")){
                Payroll pr = database.getPayroll();
                System.out.println(pr.toString());
                continue;
            }

            else if (command.equals("5")){
                Payroll pr = database.getPayroll();
                System.out.println("Enter today's date");
                String date = scanner.nextLine();
                ArrayList<Paystub> list = pr.payEmployees(date);
                database.writePaystubs(list, date);
                continue;
            }

            //More options go here

            if (command.equals("exit")){
                System.out.println("Exiting HR System.");
                break;
            }
            else {
                continue;
            }
        }

        scanner.close();
    }

}
