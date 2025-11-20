package SystemManager;

import StoreOperations.Session;
import HR.BaseEmployee;
import StoreFloor.StorePOS;

import java.util.Scanner;

import StoreFloor.CardPayment;
import StoreFloor.CashPayment;
import StoreFloor.Customer;
import StoreFloor.Item;
import StoreFloor.PaymentMethod;
import StoreFloor.StorePOS;

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
            System.out.print("5: Inventory actions\n");
            System.out.print("6: Store/Point-of-Sale\n");

            String command = scanner.nextLine();

            if (command.equals("1")) {
                HR.Util.runHR(scanner);
            } else if (command.equals("2")) {
                StoreFloor.Util.runSales(scanner);
            } else if (command.equals("3")) {
                StoreOperations.Util.runOperations(scanner);
            } else if (command.equals("5")) {
                inventory.Util.runInventory(scanner);
            }

            else if (command.equals("6")) {
                runStore(scanner);
            }
            // More options go here

            else if (command.equals("exit")) {
                System.out.println("Exiting Department Management System.");
                break;
            }
        }
        scanner.close();
    }

    private static void runStore(Scanner scanner) {
        StorePOS pos = new StorePOS();
        pos.startTransaction();

        System.out.println("Store POS - add items. Type 'done' when finished.");
        while (true) {
            System.out.print("Enter item name (or done): ");
            String name = scanner.nextLine().trim();
            if (name.equalsIgnoreCase("done"))
                break;
            System.out.print("Enter price for '" + name + "': ");
            String priceLine = scanner.nextLine().trim();
            double price = 0.0;
            try {
                price = Double.parseDouble(priceLine);
            } catch (NumberFormatException e) {
                System.out.println("Invalid price, try again.");
                continue;
            }
            Item item = new Item(name, price);
            pos.scanItem(item);
        }

        // Ask about customer and rewards membership
        System.out.print("Enter customer name (or leave blank for Guest): ");
        String custName = scanner.nextLine().trim();
        if (custName.isEmpty())
            custName = "Guest";
        System.out.print("Is the customer a rewards member? (yes/no): ");
        String rewardsLine = scanner.nextLine().trim().toLowerCase();
        boolean isMember = rewardsLine.equals("yes") || rewardsLine.equals("y");
        Customer customer = new Customer(custName, isMember);
        pos.applyAwards(customer);

        System.out.print("Enter payment method (cash/card): ");
        String method = scanner.nextLine().trim().toLowerCase();
        PaymentMethod payment;
        if (method.equals("cash")) {
            System.out.print("Enter amount given: ");
            double given = Double.parseDouble(scanner.nextLine().trim());
            payment = new CashPayment(given);
        } else {
            payment = new CardPayment();
        }

        boolean ok = pos.finalizeSale(payment);
        if (ok)
            System.out.println("Sale complete.");
        else
            System.out.println("Payment failed.");
    }
}
