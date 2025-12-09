package StoreFloor;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import Utils.Database;
import Utils.DatabaseWriter;

public class Util {
    /**
     * Main menu for cashier to choose between sale, return, or exchange
     */
    /**
     * Display items in a transaction with quantity information
     * Groups duplicate items and shows quantity
     */
    private static void displayItemsWithQuantity(List<Item> items) {
        System.out.println("\nItems in transaction:");
        
        // Create a map-like structure to count duplicates
        java.util.Map<String, Integer> itemCounts = new java.util.LinkedHashMap<>();
        java.util.Map<String, Double> itemPrices = new java.util.LinkedHashMap<>();
        java.util.List<String> orderedNames = new java.util.ArrayList<>();
        
        for (Item item : items) {
            String itemKey = item.getName() + "|" + item.getPrice();
            if (!itemCounts.containsKey(itemKey)) {
                itemCounts.put(itemKey, 0);
                itemPrices.put(itemKey, item.getPrice());
                orderedNames.add(item.getName());
            }
            itemCounts.put(itemKey, itemCounts.get(itemKey) + 1);
        }
        
        int displayIndex = 1;
        for (String itemKey : itemCounts.keySet()) {
            String[] parts = itemKey.split("\\|");
            String itemName = parts[0];
            double itemPrice = itemPrices.get(itemKey);
            int qty = itemCounts.get(itemKey);
            
            if (qty > 1) {
                System.out.println(displayIndex + ". " + itemName + " (Qty: " + qty + ") - $" + 
                    String.format("%.2f", itemPrice) + " each");
            } else {
                System.out.println(displayIndex + ". " + itemName + " - $" + 
                    String.format("%.2f", itemPrice));
            }
            displayIndex++;
        }
    }

    public static void cashierMenu(Scanner scanner) {
        // Check if someone is logged into the register before allowing any actions
        StorePOS pos = StoreOperations.Session.getCurrentPOS();
        if (pos == null || pos.getLoggedInEmployee() == null) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("ACCESS DENIED");
            System.out.println("=".repeat(50));
            System.out.println("No employee is currently logged in to a register.");
            System.out.println("Please sign in via Store Operation Actions (option 3) before using the register.");
            System.out.println("=".repeat(50));
            return;
        }

        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("CASHIER MENU");
            System.out.println("=".repeat(50));
            System.out.println("1. Process a Sale");
            System.out.println("2. Process a Return/Refund");
            System.out.println("3. Process an Exchange");
            System.out.println("4. Exit");
            System.out.print("\nSelect an option (1-4): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    runSales(scanner);
                    break;
                case "2":
                    processReturn(scanner);
                    break;
                case "3":
                    processExchange(scanner);
                    break;
                case "4":
                    System.out.println("Exiting cashier menu.");
                    return;
                default:
                    System.out.println("Invalid choice. Please select 1-4.");
            }
        }
    }

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
                pos.setPaymentMethod("CASH");
                if (ret > applied) {
                    double change = ret - applied;
                    System.out.println("Change returned: $" + String.format("%.2f", change));
                }
            } else if (method.equalsIgnoreCase("card")) {
                CardPayment cardPay = new CardPayment();
                double ret = cardPay.processPayment(toPay);
                double applied = Math.min(ret, toPay);
                paidSoFar += applied;
                pos.setPaymentMethod("CARD");
            } else if (method.equalsIgnoreCase("giftcard")) {
                System.out.print("Enter Giftcard Number: ");
                String card = scanner.nextLine().trim();
                GiftCardPayment gp = new GiftCardPayment(card);
                double ret = gp.processPayment(toPay);
                double applied = Math.min(ret, toPay);
                paidSoFar += applied;
                pos.setPaymentMethod("GIFTCARD");
            } else {
                System.out.println("Unknown payment method. Try again.");
            }
            System.out.println("Paid so far: $" + String.format("%.2f", paidSoFar) + ", Remaining: $" + String.format("%.2f", Math.max(0, remaining - paidSoFar)));
        }

        // All or enough payment collected — finalize sale
        pos.finalizeSale(paidSoFar, customer);

        System.out.println("Transaction complete.\n");
    }

    /**
     * Handles the return/refund process for a customer
     */
    public static void processReturn(Scanner scanner) {
        System.out.println("\n--- Return / Refund Process ---");
        
        // Check if POS is available and employee is logged in
        StorePOS pos = StoreOperations.Session.getCurrentPOS();
        if (pos == null || pos.getLoggedInEmployee() == null) {
            System.out.println("No employee is currently logged in to a register. Please sign in via Store Operation Actions before processing returns.");
            return;
        }

        RefundExchangeProcessor processor = pos.getRefundExchangeProcessor();
        
        System.out.print("Enter customer name: ");
        String customerName = scanner.nextLine();
        
        System.out.print("Enter transaction ID (e.g., TXN-1001): ");
        String transactionId = scanner.nextLine().trim();
        
        // Retrieve the transaction
        Transaction transaction = processor.getTransaction(transactionId);
        if (transaction == null) {
            System.out.println("Transaction not found: " + transactionId);
            return;
        }
        
        // Verify customer name matches transaction name
        String transactionCustomerName = transaction.getCustomer().getName();
        if (!customerName.equalsIgnoreCase(transactionCustomerName)) {
            System.out.println("\n--- ERROR: CUSTOMER NAME MISMATCH ---");
            System.out.println("Name entered: " + customerName);
            System.out.println("Name on transaction: " + transactionCustomerName);
            System.out.println("Customer names must match to process a return. Please verify the correct information.");
            return;
        }
        
        // Check if transaction is within return window (30 days)
        if (!transaction.isWithinReturnWindow(processor.getReturnWindowDays())) {
            System.out.println("This item is outside the " + processor.getReturnWindowDays() + "-day return window.");
            return;
        }
        
        List<Item> items = transaction.getItemsPurchased();
        displayItemsWithQuantity(items);
        
        List<Item> itemsToReturn = new ArrayList<>();
        boolean validSelection = false;
        
        while (!validSelection) {
            System.out.print("\nEnter item number to return (or 'all' for all items): ");
            String itemChoice = scanner.nextLine().trim();
            
            if (itemChoice.equalsIgnoreCase("all")) {
                itemsToReturn.addAll(items);
                validSelection = true;
            } else {
                try {
                    int itemIndex = Integer.parseInt(itemChoice) - 1;
                    
                    // Get unique item entries
                    java.util.List<String> uniqueKeys = new java.util.ArrayList<>();
                    java.util.Set<String> seenKeys = new java.util.LinkedHashSet<>();
                    for (Item item : items) {
                        String key = item.getName() + "|" + item.getPrice();
                        if (seenKeys.add(key)) {
                            uniqueKeys.add(key);
                        }
                    }
                    
                    if (itemIndex >= 0 && itemIndex < uniqueKeys.size()) {
                        String selectedKey = uniqueKeys.get(itemIndex);
                        String[] keyParts = selectedKey.split("\\|");
                        String selectedName = keyParts[0];
                        double selectedPrice = Double.parseDouble(keyParts[1]);
                        
                        // Add all items matching this selection
                        for (Item item : items) {
                            if (item.getName().equals(selectedName) && item.getPrice() == selectedPrice) {
                                itemsToReturn.add(item);
                            }
                        }
                        validSelection = true;
                    } else {
                        System.out.println("Invalid item selection. Please enter a number between 1 and " + uniqueKeys.size() + ".");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number or 'all'.");
                }
            }
        }
        
        // Calculate refund amount
        double refundAmount = 0;
        for (Item item : itemsToReturn) {
            refundAmount += item.getPrice();
        }
        
        // Check if any items are gift cards (cannot be refunded)
        List<Item> giftCardItems = new ArrayList<>();
        for (Item item : itemsToReturn) {
            if (item.getName().toLowerCase().contains("gift")) {
                giftCardItems.add(item);
            }
        }
        
        if (!giftCardItems.isEmpty()) {
            System.out.println("\n--- ERROR: GIFT CARDS CANNOT BE REFUNDED ---");
            for (Item item : giftCardItems) {
                System.out.println("Cannot refund: " + item.getName() + " - $" + 
                    String.format("%.2f", item.getPrice()) + " (Gift cards are non-refundable)");
            }
            System.out.println("Please select different items or contact management.");
            return;
        }
        
        // Check if any items have already been refunded
        List<Item> alreadyRefunded = new ArrayList<>();
        for (Item item : itemsToReturn) {
            if (processor.hasBeenRefunded(transactionId, item.getName(), item.getPrice())) {
                alreadyRefunded.add(item);
            }
        }
        
        if (!alreadyRefunded.isEmpty()) {
            System.out.println("\n--- ERROR: ITEMS ALREADY REFUNDED ---");
            for (Item item : alreadyRefunded) {
                System.out.println("Cannot refund: " + item.getName() + " - $" + 
                    String.format("%.2f", item.getPrice()) + " (Already refunded)");
            }
            System.out.println("Please select different items or contact management.");
            return;
        }
        
        System.out.println("\nRefund amount: $" + String.format("%.2f", refundAmount));
        System.out.print("How would you like to receive the refund? (cash/original/giftcard): ");
        String refundMethod = scanner.nextLine().trim().toLowerCase();
        
        if (refundMethod.equalsIgnoreCase("cash")) {
            System.out.println("Processing cash refund of $" + String.format("%.2f", refundAmount));
            System.out.println("Issuing cash refund to customer...");
        } else if (refundMethod.equalsIgnoreCase("original")) {
            System.out.println("Processing refund to original payment method: " + transaction.getPaymentMethod());
            System.out.println("Refund of $" + String.format("%.2f", refundAmount) + " will be credited to original payment method.");
        } else if (refundMethod.equalsIgnoreCase("giftcard")) {
            System.out.println("Processing gift card refund of $" + String.format("%.2f", refundAmount));
            String cardNumber = String.valueOf(System.currentTimeMillis()); // Generate unique card number
            System.out.println("Gift card number: " + cardNumber);
            
            // Create and save the gift card
            GiftCard refundCard = new GiftCard(cardNumber);
            refundCard.loadAmount(refundAmount);
            try {
                GiftCardDatabase.saveGiftCard(refundCard);
                System.out.println("Gift card saved successfully.");
            } catch (Exception e) {
                System.err.println("Error saving gift card: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid refund method.");
            return;
        }
        
        System.out.println("\n--- REFUND RECEIPT ---");
        System.out.println("Transaction ID: " + transactionId);
        System.out.println("Customer: " + customerName);
        for (Item item : itemsToReturn) {
            System.out.println("Returned: " + item.getName() + " - $" + String.format("%.2f", item.getPrice()));
        }
        System.out.println("Total Refunded: $" + String.format("%.2f", refundAmount));
        System.out.println("Refund Method: " + refundMethod);
        System.out.println("-".repeat(40));
        
        // Record the refund transaction for audit trail
        Customer refundCustomer = transaction.getCustomer();
        if (refundCustomer == null) {
            refundCustomer = new Customer(customerName, false);
        }
        processor.recordRefund(transactionId, itemsToReturn, refundAmount, refundMethod, refundCustomer);
        
        System.out.println("\nRefund process completed successfully.\n");
    }

    /**
     * Handles the exchange process for a customer
     * Use Case #19: Alternate Flow
     */
    public static void processExchange(Scanner scanner) {
        System.out.println("\n--- Exchange Process ---");
        
        // Check if POS is available and employee is logged in
        StorePOS pos = StoreOperations.Session.getCurrentPOS();
        if (pos == null || pos.getLoggedInEmployee() == null) {
            System.out.println("No employee is currently logged in to a register. Please sign in via Store Operation Actions before processing exchanges.");
            return;
        }

        RefundExchangeProcessor processor = pos.getRefundExchangeProcessor();
        
        System.out.print("Enter customer name: ");
        String customerName = scanner.nextLine();
        
        System.out.print("Enter transaction ID (e.g., TXN-1001): ");
        String transactionId = scanner.nextLine().trim();
        
        // Retrieve the transaction
        Transaction transaction = processor.getTransaction(transactionId);
        if (transaction == null) {
            System.out.println("Transaction not found: " + transactionId);
            return;
        }
        
        // Verify customer name matches transaction name
        String transactionCustomerName = transaction.getCustomer().getName();
        if (!customerName.equalsIgnoreCase(transactionCustomerName)) {
            System.out.println("\n--- ERROR: CUSTOMER NAME MISMATCH ---");
            System.out.println("Name entered: " + customerName);
            System.out.println("Name on transaction: " + transactionCustomerName);
            System.out.println("Customer names must match to process an exchange. Please verify the correct information.");
            return;
        }
        
        // Check if transaction is within return window
        if (!transaction.isWithinReturnWindow(processor.getExchangeWindowDays())) {
            System.out.println("This item is outside the " + processor.getExchangeWindowDays() + "-day exchange window.");
            return;
        }
        
        List<Item> items = transaction.getItemsPurchased();
        displayItemsWithQuantity(items);
        
        Item itemToExchange = null;
        boolean validSelection = false;
        
        while (!validSelection) {
            System.out.print("\nEnter item number to exchange: ");
            String itemChoice = scanner.nextLine().trim();
            
            try {
                int itemIndex = Integer.parseInt(itemChoice) - 1;
                
                // Get unique item entries
                java.util.List<String> uniqueKeys = new java.util.ArrayList<>();
                java.util.Set<String> seenKeys = new java.util.LinkedHashSet<>();
                for (Item item : items) {
                    String key = item.getName() + "|" + item.getPrice();
                    if (seenKeys.add(key)) {
                        uniqueKeys.add(key);
                    }
                }
                
                if (itemIndex >= 0 && itemIndex < uniqueKeys.size()) {
                    String selectedKey = uniqueKeys.get(itemIndex);
                    String[] keyParts = selectedKey.split("\\|");
                    String selectedName = keyParts[0];
                    double selectedPrice = Double.parseDouble(keyParts[1]);
                    
                    // Get the first item matching this selection
                    for (Item item : items) {
                        if (item.getName().equals(selectedName) && item.getPrice() == selectedPrice) {
                            itemToExchange = item;
                            break;
                        }
                    }
                    validSelection = true;
                } else {
                    System.out.println("Invalid item selection. Please enter a number between 1 and " + uniqueKeys.size() + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
        
        // Check if the item has already been refunded
        if (processor.hasBeenRefunded(transactionId, itemToExchange.getName(), itemToExchange.getPrice())) {
            System.out.println("\n--- ERROR: ITEM ALREADY REFUNDED ---");
            System.out.println("Cannot exchange: " + itemToExchange.getName() + " - $" + 
                String.format("%.2f", itemToExchange.getPrice()) + " (Already refunded)");
            System.out.println("An item that has been refunded cannot be exchanged.");
            System.out.println("Please select a different item or contact management.");
            return;
        }
        
        // Check if the item is a gift card (cannot be exchanged)
        if (itemToExchange.getName().toLowerCase().contains("gift")) {
            System.out.println("\n--- ERROR: GIFT CARDS CANNOT BE EXCHANGED ---");
            System.out.println("Cannot exchange: " + itemToExchange.getName() + " - $" + 
                String.format("%.2f", itemToExchange.getPrice()) + " (Gift cards are non-refundable)");
            System.out.println("Gift cards cannot be returned or exchanged.");
            System.out.println("Please select a different item or contact management.");
            return;
        }
        
        double originalPrice = itemToExchange.getPrice();
        System.out.println("\nItem to exchange: " + itemToExchange.getName() + " - $" + String.format("%.2f", originalPrice));
        
        System.out.print("Enter new item name: ");
        String newItemName = scanner.nextLine();
        
        System.out.print("Enter new item price: ");
        double newItemPrice = 0;
        try {
            newItemPrice = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price entered.");
            return;
        }
        
        double priceDifference = newItemPrice - originalPrice;
        
        System.out.println("\n--- EXCHANGE SUMMARY ---");
        System.out.println("Original item: " + itemToExchange.getName() + " - $" + String.format("%.2f", originalPrice));
        System.out.println("New item: " + newItemName + " - $" + String.format("%.2f", newItemPrice));
        
        if (priceDifference > 0) {
            System.out.println("Additional cost: $" + String.format("%.2f", priceDifference));
            System.out.print("Customer to pay additional: $" + String.format("%.2f", priceDifference) + "? (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Exchange cancelled.");
                return;
            }
        } else if (priceDifference < 0) {
            System.out.println("Customer refund: $" + String.format("%.2f", Math.abs(priceDifference)));
        }
        
        System.out.println("\n--- EXCHANGE RECEIPT ---");
        System.out.println("Transaction ID: " + transactionId);
        System.out.println("Customer: " + customerName);
        System.out.println("Item Returned: " + itemToExchange.getName() + " - $" + String.format("%.2f", originalPrice));
        System.out.println("Item Exchanged: " + newItemName + " - $" + String.format("%.2f", newItemPrice));
        if (priceDifference != 0) {
            if (priceDifference > 0) {
                System.out.println("Additional Amount Due: $" + String.format("%.2f", priceDifference));
            } else {
                System.out.println("Customer Refund: $" + String.format("%.2f", Math.abs(priceDifference)));
            }
        }
        System.out.println("-".repeat(40));
        
        // Record the exchange transaction for audit trail
        Item newItem = new Item(newItemName, newItemPrice);
        Customer exchangeCustomer = transaction.getCustomer();
        if (exchangeCustomer == null) {
            exchangeCustomer = new Customer(customerName, false);
        }
        processor.recordExchange(transactionId, itemToExchange, newItem, priceDifference, exchangeCustomer);
        
        System.out.println("\nExchange process completed successfully.\n");
    }
}