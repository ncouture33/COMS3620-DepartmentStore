package StoreFloor;

import java.util.Scanner;

public class Util {
    public static void runSales(Scanner scanner) {
        System.out.println("\n--- Point of Sale ---");

        // Require an active, logged-in POS. If none, prompt user to sign in via Store Operations.
        StorePOS pos = StoreOperations.Session.getCurrentPOS();
        if (pos == null || pos.getLoggedInEmployee() == null) {
            System.out.println("No employee is currently logged in to a register. Please sign in via Store Operation Actions before using the register.");
            return;
        }

        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();

        System.out.print("Is the customer a rewards member? (yes/no): ");
        boolean member = scanner.nextLine().equalsIgnoreCase("yes");

        Customer customer = new Customer(name, member);

        pos.startTransaction();

        while (true) {
            System.out.print("Enter item name (or 'done' to finish, or 'giftcard' to buy a gift card): ");
            String itemName = scanner.nextLine();
            if (itemName.equalsIgnoreCase("done"))
                break;
        
            double price = 0;
        
                if (itemName.equalsIgnoreCase("giftcard")) {
                    System.out.print("Enter gift card number: ");
                    String cardNumber = scanner.nextLine();
                    System.out.print("Enter gift card amount: ");
                    price = Double.parseDouble(scanner.nextLine());
                    GiftCard giftCard = pos.createGiftCard(cardNumber, price);
                    //giftCard.loadAmount(price);
                    Item giftCardItem = new Item("Gift Card", price);
                    pos.scanItem(giftCardItem);
                    System.out.println(giftCard.toString());
                    continue;
                } else {
                    System.out.print("Enter price: ");
                    price = Double.parseDouble(scanner.nextLine());
                    Item item = new Item(itemName, price);
                    // Use POS API directly (scanItem) — the logged-in employee is tracked by the POS
                    pos.scanItem(item);
                }
        }
        
        pos.applyAwards(customer);

        System.out.print("Pay with (cash/card/giftcard): ");
        String method = scanner.nextLine();

        if (method.equalsIgnoreCase("cash")) {
            System.out.print("Enter cash amount: ");
            double cash = Double.parseDouble(scanner.nextLine());
            PaymentMethod payment = new CashPayment(cash);
            pos.finalizeSale(payment);
        } else if(method.equalsIgnoreCase("card")) {
            PaymentMethod payment = new CardPayment();
            pos.finalizeSale(payment);
        }else {
            System.out.println("Enter Giftcard Number: ");
            String card = scanner.nextLine();
            PaymentMethod payment = new GiftCardPayment(card);
            pos.finalizeSale(payment);
        }

        System.out.println("Transaction complete.\n");
    }
}