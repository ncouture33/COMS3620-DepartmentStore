package SystemManager;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Department Management System is running...");
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("1: HR related actions\n");

            String command = scanner.nextLine();

            if (command.equals("1")){
                HR.Util.runHR();
                continue;
            }
            else if (command.equals("2")){
                StoreFloor.Util.runSales(scanner);
                continue;
            }
            //More options go here

            if (command.equals("exit")){
                System.out.println("Exiting Department Management System.");
                break;
            }
            else {
                continue;
            }
        }

        scanner.close();
    }
}

