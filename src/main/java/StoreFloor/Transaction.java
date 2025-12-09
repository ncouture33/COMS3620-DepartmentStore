package StoreFloor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a completed transaction in the store POS system
 * Stores transaction details including items purchased, amounts, and timestamps
 */
public class Transaction {
    private String transactionId;
    private List<Item> itemsPurchased;
    private double totalAmount;
    private double amountPaid;
    private double changeGiven;
    private long transactionDate;
    private String paymentMethod;
    private Customer customer;

    public Transaction(String transactionId, List<Item> itemsPurchased, double totalAmount, 
                       double amountPaid, double changeGiven, long transactionDate, 
                       String paymentMethod, Customer customer) {
        this.transactionId = transactionId;
        this.itemsPurchased = new ArrayList<>(itemsPurchased);
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.changeGiven = changeGiven;
        this.transactionDate = transactionDate;
        this.paymentMethod = paymentMethod;
        this.customer = customer;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public List<Item> getItemsPurchased() {
        return new ArrayList<>(itemsPurchased);
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getChangeGiven() {
        return changeGiven;
    }

    public long getTransactionDate() {
        return transactionDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Customer getCustomer() {
        return customer;
    }

    /**
     * Checks if this transaction is within the return window
     * Default return window is 30 days
     */
    public boolean isWithinReturnWindow(int daysAllowed) {
        long returnDeadlineMillis = transactionDate + (daysAllowed * 24 * 60 * 60 * 1000L);
        return System.currentTimeMillis() < returnDeadlineMillis;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + transactionId + '\'' +
                ", date=" + new java.util.Date(transactionDate) +
                ", items=" + itemsPurchased.size() +
                ", total=$" + String.format("%.2f", totalAmount) +
                '}';
    }
}
