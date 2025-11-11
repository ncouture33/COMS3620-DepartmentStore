package StoreFloor;

import java.util.Scanner;

public class Util {
    public static void runSales(Scanner scanner) {
        System.out.println("\n--- Point of Sale ---");

        StorePOS pos = new StorePOS();
        Cashier cashier = new Cashier(0,"John", "Doe", 03,1234567890,12.0,18);

        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();

        System.out.print("Is the customer a rewards member? (yes/no): ");
        boolean member = scanner.nextLine().equalsIgnoreCase("yes");

        Customer customer = new Customer(name, member);

        pos.startTransaction();

        while (true) {
            System.out.print("Enter item name (or 'done' to finish): ");
            String itemName = scanner.nextLine();
            if (itemName.equalsIgnoreCase("done"))
                break;

            System.out.print("Enter price: ");
            double price = Double.parseDouble(scanner.nextLine());
            Item item = new Item(itemName, price);

            cashier.ringUpItem(item, pos);
        }

        cashier.applyAwards(customer, pos);

        System.out.print("Pay with (cash/card): ");
        String method = scanner.nextLine();

        if (method.equalsIgnoreCase("cash")) {
            System.out.print("Enter cash amount: ");
            double cash = Double.parseDouble(scanner.nextLine());
            PaymentMethod payment = new CashPayment(cash);
            cashier.completeSale(pos, payment);
        } else {
            PaymentMethod payment = new CardPayment();
            cashier.completeSale(pos, payment);
        }

        System.out.println("Transaction complete.\n");
    }
}
