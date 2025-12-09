package SystemManager;

import java.util.Scanner;

import HR.BaseEmployee;
import StoreFloor.StorePOS;
import StoreOperations.Session;

public class Main {
    public static void main(String[] args) {
        System.out.println("Department Management System is running...");
        Scanner scanner = new Scanner(System.in);

        while (true) {

            // show active register status when someone is logged in
            StorePOS active = Session.getCurrentPOS();
            if (active != null && active.getLoggedInEmployee() != null) {
                BaseEmployee be = active.getLoggedInEmployee();
                String user = be.getUsername() == null ? (be.getFName() + " " + be.getLName())
                        : be.getUsername() + " (" + be.getFName() + " " + be.getLName() + ")";
                System.out.println("Active register: " + user);
            }

            System.out.print("1: HR related actions\n");
            System.out.print("2: Store Floor actions\n");
            System.out.print("3: Store Operation Actions\n");
            System.out.print("4: Security related actions\n");
            System.out.print("5: Inventory actions\n");

            String command = scanner.nextLine();

            if (command.equals("1")) {
                HR.Util.runHR(scanner);
            } else if (command.equals("2")) {
                StoreFloor.Util.runSales(scanner);
            } else if (command.equals("3")) {
                StoreOperations.Util.runOperations(scanner);
            } else if (command.equals("4")) {
                Security.Util.runSecurity(scanner);
            } else if (command.equals("5")) {
                inventory.Util.runInventory(scanner);
            }
            // More options go here

            else if (command.equals("exit")) {
                System.out.println("Exiting Department Management System.");
                break;
            }
        }
        scanner.close();
    }
}
