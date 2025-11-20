package StoreFloor;

import java.util.Scanner;

import Utils.Database;
import Utils.DatabaseWriter;

public class Util {
    public static void runSales(Scanner scanner) {
        System.out.println("\n--- Point of Sale ---");
        DatabaseWriter database = new Database();

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

        if (member){
            System.out.print("Enter phone number: ");
            String phoneNumber = scanner.nextLine();
            
            Rewards rewards = database.getCustomerRewards(phoneNumber);
            if (rewards == null){
                System.out.println("No rewards account found for phone number " + phoneNumber);
            }
            else{
                customer.setRewards(rewards);
                System.out.println("Rewards ID " + rewards.getId() + " linked to customer " + name + " with phone number " + phoneNumber);
            }  
        }
        else{
            System.out.print("Would you like to join the rewards program? (yes/no): ");
            boolean answer = scanner.nextLine().equalsIgnoreCase("yes");
            if (answer){
                //add the customer to the rewards program
                System.out.print("Enter phone number: ");
                String phoneNumber = scanner.nextLine();
                System.out.print("Enter email: ");
                String email = scanner.nextLine();
                customer = new Customer(name, true);
                Rewards rewards = new Rewards(-1, 0, email, phoneNumber);
                int id = database.generateCustomerRewardsID();
                rewards.setID(id);
                customer.setRewards(rewards);
                database.addCustomerToRewardsProgram(customer);
                System.out.println("Customer " + name + " added to rewards program with phone number " + phoneNumber);
            }
        }

        

        pos.startTransaction();

        // allocate a per-transaction sequence for gift card IDs so multiple cards
        // in a single transaction get unique IDs even before persisting to disk
        int nextGiftCardId = GiftCardDatabase.getNextGiftCardID();

        while (true) {
            System.out.print("Enter item name (or 'done' to finish, or 'giftcard' to buy a gift card): ");
            String itemName = scanner.nextLine();
            if (itemName.equalsIgnoreCase("done"))
                break;
        
            double price = 0;
        
            if (itemName.equalsIgnoreCase("giftcard")) {
                String cardNumber = String.valueOf(nextGiftCardId++);
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

        double paidSoFar = 0.0;
        double remaining = pos.total; // package-private access: use getter? total is protected; using pos.total directly in same package is ok

        while (paidSoFar < remaining) {
            System.out.print("Pay with (cash/card/giftcard) or type 'cancel' to abort: ");
            String method = scanner.nextLine().trim();
            if (method.equalsIgnoreCase("cancel")){
                System.out.println("Payment cancelled. Transaction aborted.");
                return;
            }

            double toPay = remaining - paidSoFar;

            if (method.equalsIgnoreCase("cash")) {
                System.out.print("Enter cash amount: ");
                String line = scanner.nextLine().trim();
                double cash;
                try {
                    cash = Double.parseDouble(line);
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid amount. Please enter a numeric value.");
                    continue;
                }
                CashPayment cp = new CashPayment(cash);
                double ret = cp.processPayment(toPay);
                double applied = Math.min(ret, toPay);
                paidSoFar += applied;
                if (ret > applied) {
                    double change = ret - applied;
                    System.out.println("Change returned: $" + String.format("%.2f", change));
                }
            } else if (method.equalsIgnoreCase("card")) {
                CardPayment cardPay = new CardPayment();
                double ret = cardPay.processPayment(toPay);
                double applied = Math.min(ret, toPay);
                paidSoFar += applied;
            } else if (method.equalsIgnoreCase("giftcard")) {
                System.out.print("Enter Giftcard Number: ");
                String card = scanner.nextLine().trim();
                GiftCardPayment gp = new GiftCardPayment(card);
                double ret = gp.processPayment(toPay);
                double applied = Math.min(ret, toPay);
                paidSoFar += applied;
            } else {
                System.out.println("Unknown payment method. Try again.");
            }
            System.out.println("Paid so far: $" + String.format("%.2f", paidSoFar) + ", Remaining: $" + String.format("%.2f", Math.max(0, remaining - paidSoFar)));
        }

        // All or enough payment collected — finalize sale
        pos.finalizeSale(paidSoFar, customer);

        System.out.println("Transaction complete.\n");
    }
}