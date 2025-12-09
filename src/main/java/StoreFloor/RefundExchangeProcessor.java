package StoreFloor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

/**
 * Handles the return and exchange operations for a POS system
 * Implements Use Case #19: Returning / Exchanging a Product
 */
public class RefundExchangeProcessor {
    private static final int DEFAULT_RETURN_WINDOW_DAYS = 30;
    private static final int DEFAULT_EXCHANGE_WINDOW_DAYS = 30;
    private static final String TRANSACTIONS_FILE = "transactions.txt";
    private static final String COUNTER_FILE = "transaction_counter.txt";
    private List<Transaction> transactionHistory = new ArrayList<>();
    private static int nextTransactionNumber = 1000;

    /**
     * Constructor - loads existing transactions from file and initializes counter
     */
    public RefundExchangeProcessor() {
        loadTransactionCounter();
        loadTransactions();
    }

    /**
     * Register a completed transaction in the history
     */
    public void recordTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
        saveTransaction(transaction);
        System.out.println("Transaction recorded: " + transaction.getTransactionId());
    }

    /**
     * Retrieve a transaction by its ID
     */
    public Transaction getTransaction(String transactionId) {
        // First check in memory
        for (Transaction t : transactionHistory) {
            if (t.getTransactionId().equals(transactionId)) {
                return t;
            }
        }
        // If not in memory, reload from file
        loadTransactions();
        for (Transaction t : transactionHistory) {
            if (t.getTransactionId().equals(transactionId)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Check if an item has already been refunded from an original transaction
     * Returns true if the item (by name and price) has been refunded
     */
    public boolean hasBeenRefunded(String originalTransactionId, String itemName, double itemPrice) {
        // Load all transactions to get latest data
        loadTransactions();
        
        for (Transaction t : transactionHistory) {
            // Look for refund transactions that reference this original transaction
            if (t.getTransactionId().startsWith("TXN-REFUND-")) {
                // Check if any refunded items match
                for (Item refundedItem : t.getItemsPurchased()) {
                    if (refundedItem.getName().equals(itemName) && 
                        refundedItem.getPrice() == itemPrice) {
                        // Now verify this refund was for the original transaction
                        // by checking if the same item was in the original transaction
                        Transaction original = getTransaction(originalTransactionId);
                        if (original != null) {
                            for (Item origItem : original.getItemsPurchased()) {
                                if (origItem.getName().equals(itemName) && 
                                    origItem.getPrice() == itemPrice) {
                                    return true; // Item was already refunded
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the total amount already refunded for a transaction
     */
    public double getTotalRefundedAmount(String originalTransactionId) {
        loadTransactions();
        double totalRefunded = 0;
        
        for (Transaction t : transactionHistory) {
            if (t.getTransactionId().startsWith("TXN-REFUND-")) {
                for (Item item : t.getItemsPurchased()) {
                    totalRefunded += item.getPrice();
                }
            }
        }
        return totalRefunded;
    }

    /**
     * Save a single transaction to file
     */
    private void saveTransaction(Transaction transaction) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE, true))) {
            writer.write(transactionToString(transaction));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }

    /**
     * Load all transactions from file
     */
    private void loadTransactions() {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) {
            return;
        }

        transactionHistory.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Transaction transaction = stringToTransaction(line);
                if (transaction != null) {
                    transactionHistory.add(transaction);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
    }

    /**
     * Load the transaction counter from file
     */
    private void loadTransactionCounter() {
        File file = new File(COUNTER_FILE);
        if (!file.exists()) {
            nextTransactionNumber = 1000;
            saveTransactionCounter();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(COUNTER_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                try {
                    nextTransactionNumber = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    nextTransactionNumber = 1000;
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transaction counter: " + e.getMessage());
            nextTransactionNumber = 1000;
        }
    }

    /**
     * Save the transaction counter to file
     */
    private void saveTransactionCounter() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COUNTER_FILE))) {
            writer.write(String.valueOf(nextTransactionNumber));
        } catch (IOException e) {
            System.err.println("Error saving transaction counter: " + e.getMessage());
        }
    }

    /**
     * Get the next transaction ID and increment the counter
     */
    public synchronized String getNextTransactionId() {
        String transactionId = "TXN-" + nextTransactionNumber;
        nextTransactionNumber++;
        saveTransactionCounter();
        return transactionId;
    }

    /**
     * Convert a transaction to a string for file storage
     * Format: ID|timestamp|totalAmount|amountPaid|changeGiven|paymentMethod|customerName|itemCount|item1Name:price|item2Name:price...
     */
    private String transactionToString(Transaction transaction) {
        StringBuilder sb = new StringBuilder();
        sb.append(transaction.getTransactionId()).append("|");
        sb.append(transaction.getTransactionDate()).append("|");
        sb.append(transaction.getTotalAmount()).append("|");
        sb.append(transaction.getAmountPaid()).append("|");
        sb.append(transaction.getChangeGiven()).append("|");
        sb.append(transaction.getPaymentMethod()).append("|");
        
        Customer customer = transaction.getCustomer();
        String customerName = (customer != null) ? customer.getName() : "Guest";
        sb.append(customerName).append("|");
        
        List<Item> items = transaction.getItemsPurchased();
        sb.append(items.size()).append("|");
        
        for (Item item : items) {
            sb.append(item.getName()).append(":").append(item.getPrice()).append("|");
        }
        
        return sb.toString();
    }

    /**
     * Convert a string from file to a Transaction object
     */
    private Transaction stringToTransaction(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 8) {
                return null;
            }
            
            String transactionId = parts[0];
            long timestamp = Long.parseLong(parts[1]);
            double totalAmount = Double.parseDouble(parts[2]);
            double amountPaid = Double.parseDouble(parts[3]);
            double changeGiven = Double.parseDouble(parts[4]);
            String paymentMethod = parts[5];
            String customerName = parts[6];
            int itemCount = Integer.parseInt(parts[7]);
            
            List<Item> items = new ArrayList<>();
            for (int i = 0; i < itemCount && (8 + i) < parts.length; i++) {
                String itemData = parts[8 + i];
                String[] itemParts = itemData.split(":");
                if (itemParts.length == 2) {
                    String itemName = itemParts[0];
                    double itemPrice = Double.parseDouble(itemParts[1]);
                    items.add(new Item(itemName, itemPrice));
                }
            }
            
            Customer customer = new Customer(customerName, false);
            
            return new Transaction(transactionId, items, totalAmount, amountPaid, 
                                   changeGiven, timestamp, paymentMethod, customer);
        } catch (Exception e) {
            System.err.println("Error parsing transaction: " + e.getMessage());
            return null;
        }
    }

    /**
     * Initiates a return/exchange process
     * Main Success Scenario:
     * 1. Customer provides product and receipt
     * 2. Cashier enters transaction ID
     * 3. Display items from that transaction
     * 4. Cashier selects which item(s) to return
     * 5. Determine refund method (cash, original method, gift card)
     * 6. Process refund/exchange
     */
    public boolean processReturnExchange(Cashier cashier, Customer customer, String transactionId, 
                                         Scanner inputScanner) {
        // Step 2: Retrieve transaction
        Transaction originalTransaction = getTransaction(transactionId);
        
        if (originalTransaction == null) {
            System.out.println("ERROR: Transaction ID not found: " + transactionId);
            return false;
        }

        // Check if transaction is within return window
        if (!originalTransaction.isWithinReturnWindow(DEFAULT_RETURN_WINDOW_DAYS)) {
            System.out.println("ERROR: This transaction is outside the " + DEFAULT_RETURN_WINDOW_DAYS + 
                             " day return window.");
            return false;
        }

        System.out.println("\n--- RETURN/EXCHANGE PROCESS ---");
        System.out.println("Transaction ID: " + originalTransaction.getTransactionId());
        System.out.println("Original Purchase Date: " + originalTransaction.getTransactionDate());
        System.out.println("Original Total: $" + String.format("%.2f", originalTransaction.getTotalAmount()));

        // Step 3: Display items purchased in this transaction
        List<Item> itemsPurchased = originalTransaction.getItemsPurchased();
        System.out.println("\nItems in this transaction:");
        for (int i = 0; i < itemsPurchased.size(); i++) {
            Item item = itemsPurchased.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " - $" + 
                             String.format("%.2f", item.getPrice()));
        }

        // Step 4: Cashier selects which item(s) to return
        System.out.println("\nEnter item numbers to return (comma-separated, e.g., 1,3):");
        String itemSelection = inputScanner.nextLine();
        List<Item> returningItems = parseItemSelection(itemSelection, itemsPurchased);

        if (returningItems.isEmpty()) {
            System.out.println("ERROR: No valid items selected for return.");
            return false;
        }

        double refundAmount = calculateRefundAmount(returningItems);
        
        // Step 5: Determine refund method
        System.out.println("\nHow would the customer like the refund?");
        System.out.println("1. Original payment method (" + originalTransaction.getPaymentMethod() + ")");
        System.out.println("2. Cash");
        System.out.println("3. Gift Card");
        System.out.print("Select refund method (1-3): ");
        
        String refundMethodChoice = inputScanner.nextLine();
        String refundMethod = parseRefundMethod(refundMethodChoice, 
                                               originalTransaction.getPaymentMethod());

        if (refundMethod == null) {
            System.out.println("ERROR: Invalid refund method selected.");
            return false;
        }

        // Step 6: Process the refund
        System.out.println("\nRefund Details:");
        System.out.println("Items being returned: " + returningItems.size());
        System.out.println("Refund Amount: $" + String.format("%.2f", refundAmount));
        System.out.println("Refund Method: " + refundMethod);

        System.out.print("Confirm refund? (yes/no): ");
        String confirmation = inputScanner.nextLine();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Refund cancelled.");
            return false;
        }

        // Process the refund (Step 6)
        processRefund(refundAmount, refundMethod, customer);

        // Step 8: Generate return receipt
        printReturnReceipt(originalTransaction, returningItems, refundAmount, refundMethod);

        return true;
    }

    /**
     * Processes an exchange instead of a refund
     * Alternate Flow: Customer wants to exchange product
     */
    public boolean processExchange(Cashier cashier, Customer customer, String transactionId,
                                   Scanner inputScanner, StorePOS posSystem) {
        // Steps 1-4: Same as return
        Transaction originalTransaction = getTransaction(transactionId);
        
        if (originalTransaction == null) {
            System.out.println("ERROR: Transaction ID not found: " + transactionId);
            return false;
        }

        if (!originalTransaction.isWithinReturnWindow(DEFAULT_RETURN_WINDOW_DAYS)) {
            System.out.println("ERROR: This transaction is outside the return window.");
            return false;
        }

        System.out.println("\n--- EXCHANGE PROCESS ---");
        System.out.println("Transaction ID: " + originalTransaction.getTransactionId());

        // Display items to be exchanged
        List<Item> itemsPurchased = originalTransaction.getItemsPurchased();
        System.out.println("\nItems in this transaction:");
        for (int i = 0; i < itemsPurchased.size(); i++) {
            Item item = itemsPurchased.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " - $" + 
                             String.format("%.2f", item.getPrice()));
        }

        System.out.println("\nEnter item numbers to exchange (comma-separated):");
        String itemSelection = inputScanner.nextLine();
        List<Item> exchangingItems = parseItemSelection(itemSelection, itemsPurchased);

        if (exchangingItems.isEmpty()) {
            System.out.println("ERROR: No valid items selected for exchange.");
            return false;
        }

        double exchangeValue = calculateRefundAmount(exchangingItems);

        // Scan new item to exchange for
        System.out.println("\nEnter the item code for the new item:");
        System.out.println("(This would normally scan the barcode)");
        System.out.print("New item code: ");
        String newItemCode = inputScanner.nextLine();

        // Create a new item (in real system, would look up from inventory)
        System.out.print("New item name: ");
        String newItemName = inputScanner.nextLine();
        System.out.print("New item price: $");
        double newItemPrice = Double.parseDouble(inputScanner.nextLine());

        Item newItem = new Item(newItemName, newItemPrice);
        double priceDifference = newItemPrice - exchangeValue;

        System.out.println("\nExchange Details:");
        System.out.println("Items being exchanged: " + exchangingItems.size());
        System.out.println("Exchange value: $" + String.format("%.2f", exchangeValue));
        System.out.println("New item: " + newItem.getName() + " - $" + 
                         String.format("%.2f", newItemPrice));

        if (priceDifference > 0) {
            System.out.println("Additional charge: $" + String.format("%.2f", priceDifference));
        } else if (priceDifference < 0) {
            System.out.println("Refund due: $" + String.format("%.2f", -priceDifference));
        }

        System.out.print("Confirm exchange? (yes/no): ");
        String confirmation = inputScanner.nextLine();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Exchange cancelled.");
            return false;
        }

        // Process exchange
        if (priceDifference > 0) {
            System.out.println("Customer owes additional: $" + 
                             String.format("%.2f", priceDifference));
            // In real system, collect additional payment
        } else if (priceDifference < 0) {
            processRefund(-priceDifference, "EXCHANGE_CREDIT", customer);
        }

        // Generate exchange receipt
        printExchangeReceipt(originalTransaction, exchangingItems, newItem, priceDifference);

        return true;
    }

    /**
     * Parse item selection from user input
     */
    private List<Item> parseItemSelection(String selection, List<Item> availableItems) {
        List<Item> selected = new ArrayList<>();
        try {
            String[] indices = selection.split(",");
            for (String idx : indices) {
                int index = Integer.parseInt(idx.trim()) - 1;
                if (index >= 0 && index < availableItems.size()) {
                    selected.add(availableItems.get(index));
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input format.");
        }
        return selected;
    }

    /**
     * Calculate total refund amount from items
     */
    private double calculateRefundAmount(List<Item> items) {
        double total = 0;
        for (Item item : items) {
            total += item.getPrice();
        }
        return total;
    }

    /**
     * Parse refund method selection
     */
    private String parseRefundMethod(String choice, String originalMethod) {
        switch (choice.trim()) {
            case "1":
                return originalMethod;
            case "2":
                return "CASH";
            case "3":
                return "GIFT_CARD";
            default:
                return null;
        }
    }

    /**
     * Process the refund (actual money handling logic)
     */
    private void processRefund(double amount, String method, Customer customer) {
        System.out.println("\nProcessing refund...");
        System.out.println("Method: " + method);
        System.out.println("Amount: $" + String.format("%.2f", amount));

        switch (method) {
            case "CASH":
                System.out.println("Dispensing $" + String.format("%.2f", amount) + " in cash");
                break;
            case "ORIGINAL_PAYMENT_METHOD":
                System.out.println("Refunding $" + String.format("%.2f", amount) + 
                                 " to original payment method");
                break;
            case "GIFT_CARD":
                System.out.println("Creating gift card with $" + String.format("%.2f", amount));
                break;
            case "EXCHANGE_CREDIT":
                System.out.println("Applying $" + String.format("%.2f", amount) + 
                                 " as credit for exchange");
                break;
        }

        System.out.println("Refund processed successfully!");
    }

    /**
     * Print receipt for return transaction
     */
    private void printReturnReceipt(Transaction original, List<Item> returnedItems, 
                                    double refundAmount, String refundMethod) {
        System.out.println("\n--- RETURN RECEIPT ---");
        System.out.println("Original Transaction ID: " + original.getTransactionId());
        System.out.println("\nItems Returned:");
        for (Item item : returnedItems) {
            System.out.println("- " + item.getName() + ": $" + 
                             String.format("%.2f", item.getPrice()));
        }
        System.out.println("----------------------------");
        System.out.println("Refund Amount: $" + String.format("%.2f", refundAmount));
        System.out.println("Refund Method: " + refundMethod);
        System.out.println("----------------------------\n");
    }

    /**
     * Print receipt for exchange transaction
     */
    private void printExchangeReceipt(Transaction original, List<Item> exchangedItems, 
                                      Item newItem, double priceDifference) {
        System.out.println("\n--- EXCHANGE RECEIPT ---");
        System.out.println("Original Transaction ID: " + original.getTransactionId());
        System.out.println("\nItems Exchanged:");
        for (Item item : exchangedItems) {
            System.out.println("- " + item.getName() + ": $" + 
                             String.format("%.2f", item.getPrice()));
        }
        System.out.println("\nNew Item Received:");
        System.out.println("- " + newItem.getName() + ": $" + 
                         String.format("%.2f", newItem.getPrice()));
        System.out.println("----------------------------");
        if (priceDifference > 0) {
            System.out.println("Additional Amount Due: $" + String.format("%.2f", priceDifference));
        } else if (priceDifference < 0) {
            System.out.println("Refund Due: $" + String.format("%.2f", -priceDifference));
        } else {
            System.out.println("Even exchange");
        }
        System.out.println("----------------------------\n");
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    /**
     * Record a refund transaction
     * Creates a new transaction entry with "REFUND" prefix to track refunds separately
     * This maintains an audit trail while preventing double refunds
     */
    public void recordRefund(String originalTransactionId, List<Item> refundedItems, 
                             double refundAmount, String refundMethod, Customer customer) {
        String refundTransactionId = "TXN-REFUND-" + System.currentTimeMillis();
        
        // Create a refund transaction (negative amount to indicate refund)
        Transaction refundTransaction = new Transaction(
            refundTransactionId,
            refundedItems,
            refundAmount,
            0.0,  // No payment received on refund
            refundAmount,  // Refund amount given
            System.currentTimeMillis(),
            "REFUND_" + refundMethod.toUpperCase(),  // Mark refund method
            customer
        );
        
        recordTransaction(refundTransaction);
        System.out.println("Refund recorded: " + refundTransactionId + " for original transaction " + originalTransactionId);
    }

    /**
     * Record an exchange transaction
     * Creates a new transaction entry to track the exchange
     */
    public void recordExchange(String originalTransactionId, Item returnedItem, Item newItem, 
                               double priceDifference, Customer customer) {
        String exchangeTransactionId = "TXN-EXCHANGE-" + System.currentTimeMillis();
        
        java.util.List<Item> exchangeItems = new java.util.ArrayList<>();
        exchangeItems.add(returnedItem);
        exchangeItems.add(newItem);
        
        // Create exchange transaction
        Transaction exchangeTransaction = new Transaction(
            exchangeTransactionId,
            exchangeItems,
            Math.abs(priceDifference),
            0.0,  // Customer may owe money or receive refund
            priceDifference > 0 ? priceDifference : 0.0,  // Amount customer pays (if positive)
            System.currentTimeMillis(),
            "EXCHANGE",
            customer
        );
        
        recordTransaction(exchangeTransaction);
        System.out.println("Exchange recorded: " + exchangeTransactionId + " for original transaction " + originalTransactionId);
    }

    /**
     * Get the return window in days
     */
    public int getReturnWindowDays() {
        return DEFAULT_RETURN_WINDOW_DAYS;
    }

    /**
     * Get the exchange window in days
     */
    public int getExchangeWindowDays() {
        return DEFAULT_EXCHANGE_WINDOW_DAYS;
    }
}
