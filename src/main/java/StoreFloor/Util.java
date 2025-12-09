package StoreFloor;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import Utils.Database;
import Utils.DatabaseWriter;
import inventory.io.InventoryFileStore;
import inventory.model.Product;

public class Util {

    public static void runSales(Scanner scanner) {
        while (true) {
            System.out.println("\nStore Floor - choose an option:");
            System.out.println("1: Point of Sale");
            System.out.println("2: Alterations and Tailoring");
            System.out.println("3: Complete Alteration");
            System.out.println("4: Personal Shopping Appointments");
            System.out.println("5: Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                runPOS(scanner);
            } else if (choice.equals("2")) {
                runAlterations(scanner);
            } else if (choice.equals("3")) {
                completeAlteration(scanner);
            } else if (choice.equals("4")) {
                AppointmentUI appointmentUI = new AppointmentUI(scanner);
                appointmentUI.showAppointmentMenu();
            } else if (choice.equals("5")) {
                break;
            }
        }
    }

    public static void runPOS(Scanner scanner) {
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

        if (member) {
            System.out.print("Enter phone number: ");
            String phoneNumber = scanner.nextLine();

            Rewards rewards = database.getCustomerRewards(phoneNumber);
            if (rewards == null) {
                System.out.println("No rewards account found for phone number " + phoneNumber);
            } else {
                customer.setRewards(rewards);
                System.out.println("Rewards ID " + rewards.getId() + " linked to customer " + name + " with phone number " + phoneNumber);
            }
        } else {
            System.out.print("Would you like to join the rewards program? (yes/no): ");
            boolean answer = scanner.nextLine().equalsIgnoreCase("yes");
            if (answer) {
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
            if (itemName.equalsIgnoreCase("done")) {
                break;
            }

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

        double paidSoFar = processPayment(scanner, pos.total);
        if (paidSoFar < 0) {
            System.out.println("Payment cancelled. Transaction aborted.");
            return;
        }

        pos.finalizeSale(paidSoFar, customer);

        System.out.println("Transaction complete.\n");
    }

    public static double processPayment(Scanner scanner, double totalAmount) {
        double paidSoFar = 0.0;
        double remaining = totalAmount;

        while (paidSoFar < remaining) {
            System.out.print("Pay with (cash/card/giftcard) or type 'cancel' to abort: ");
            String method = scanner.nextLine().trim();
            if (method.equalsIgnoreCase("cancel")) {
                return -1;
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

        return paidSoFar;
    }

    public static void runAlterations(Scanner scanner) {
        System.out.println("\n--- Alterations and Tailoring Services ---");
        DatabaseWriter database = new Database();

        System.out.print("Enter customer name: ");
        String customerName = scanner.nextLine().trim();

        System.out.print("Enter customer phone number: ");
        String customerPhone = scanner.nextLine().trim();

        System.out.print("Enter item SKU: ");
        String itemSKU = scanner.nextLine().trim();

        InventoryFileStore inventoryStore = new InventoryFileStore(Paths.get("."));
        Map<String, Product> productCatalog = inventoryStore.loadProductCatalog();

        if (!productCatalog.containsKey(itemSKU)) {
            System.out.println("Error: SKU '" + itemSKU + "' not found in the system.");
            System.out.println("Alteration request cancelled.");
            return;
        }

        Product product = productCatalog.get(itemSKU);
        System.out.println("SKU validated: " + product.getName() + " - $" + product.getUnitPrice());

        System.out.print("Enter purchase date: ");
        String purchaseDate = scanner.nextLine().trim();

        System.out.println("\n--- Tailor Consultation ---");

        System.out.print("Enter alteration instructions: ");
        String alterationInstructions = scanner.nextLine().trim();

        System.out.print("Enter measurements: ");
        String measurements = scanner.nextLine().trim();

        System.out.print("Enter estimated cost: ");
        double cost;
        try {
            cost = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid cost. Aborting alteration request.");
            return;
        }

        System.out.print("Enter estimated completion date: ");
        String completionDate = scanner.nextLine().trim();

        System.out.print("Does the customer approve the alterations and cost? (yes/no): ");
        boolean approved = scanner.nextLine().trim().equalsIgnoreCase("yes");

        if (!approved) {
            System.out.println("Customer declined alterations. Request cancelled.");
            return;
        }

        double paidAmount = processPayment(scanner, cost);
        if (paidAmount < 0) {
            System.out.println("Payment cancelled. Alteration request aborted.");
            return;
        }

        String trackingNumber = database.generateAlterationTrackingNumber();

        AlterationRequest request = new AlterationRequest(
                trackingNumber,
                customerName,
                customerPhone,
                itemSKU,
                purchaseDate,
                alterationInstructions,
                measurements,
                cost,
                completionDate,
                "In Progress");

        database.writeAlterationRequest(request);

        System.out.println("\n--- Alteration Claim Ticket ---");
        System.out.println("Tracking Number: " + trackingNumber);
        System.out.println("Customer: " + customerName);
        System.out.println("Phone: " + customerPhone);
        System.out.println("Item SKU: " + itemSKU);
        System.out.println("Alterations: " + alterationInstructions);
        System.out.println("Measurements: " + measurements);
        System.out.println("Cost: $" + String.format("%.2f", cost));
        System.out.println("Estimated Completion: " + completionDate);
        System.out.println("Status: In Progress");
        System.out.println("\nGarment logged in alterations inventory.");
        System.out.println("Request added to tailor's work queue.");
        System.out.println("Customer copy of claim ticket generated.\n");
    }

    public static void completeAlteration(Scanner scanner) {
        System.out.println("\n--- Complete Alteration ---");
        DatabaseWriter database = new Database();

        System.out.print("Enter tracking number: ");
        String trackingNumber = scanner.nextLine().trim();

        AlterationRequest request = database.getAlterationByTrackingNumber(trackingNumber);

        if (request == null) {
            System.out.println("Error: Tracking number '" + trackingNumber + "' not found.");
            return;
        }

        if (request.getStatus().equals("Completed")) {
            System.out.println("This alteration is already completed.");
            return;
        }

        System.out.println("\nAlteration Details:");
        System.out.println("Customer: " + request.getCustomerName());
        System.out.println("Phone: " + request.getCustomerPhone());
        System.out.println("Item SKU: " + request.getItemSKU());
        System.out.println("Alterations: " + request.getAlterationInstructions());
        System.out.println("Cost: $" + String.format("%.2f", request.getCost()));
        System.out.println("Current Status: " + request.getStatus());

        System.out.print("\nMark this alteration as completed? (yes/no): ");
        boolean confirm = scanner.nextLine().trim().equalsIgnoreCase("yes");

        if (!confirm) {
            System.out.println("Operation cancelled.");
            return;
        }

        boolean success = database.updateAlterationStatus(trackingNumber, "Completed");

        if (success) {
            System.out.println("\nAlteration " + trackingNumber + " marked as completed.");
            System.out.println("Customer " + request.getCustomerName() + " can pick up their item.");
        } else {
            System.out.println("\nError: Failed to update alteration status.");
        }
    }
}
