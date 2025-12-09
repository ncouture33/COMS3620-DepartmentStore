package StoreFloor;

import java.util.ArrayList;
import java.util.List;

public class StorePOS extends AbstractPOSSystem {
    private RefundExchangeProcessor refundExchangeProcessor = new RefundExchangeProcessor();
    private String lastPaymentMethod = "UNKNOWN";  // Track the payment method used

    @Override
    public void applyAwards(Customer customer) {
        // if (customer.isRewardsMember()) {
        //     //give customer points
        //     //add discount logic here
        //     System.out.println("Applied rewards for customer: " + customer.getName());
        // } else {
        //     System.out.println("Customer is not a rewards member");
        // }
    }

    @Override
    protected void printReceipt() {
        System.out.println("\n--- RECEIPT ---");

        for (Item item : currentSale) {
            System.out.println(item.getName() + " - $" + String.format("%.2f", item.getPrice()));
        }

        System.out.println("----------------------------");

        System.out.println("Transaction Total: $" + String.format("%.2f", total));
        System.out.println("Total Paid:        $" + String.format("%.2f", totalPaid));

        if (changeReturned > 0) {
            System.out.println("Change Returned:   $" + String.format("%.2f", changeReturned));
        }

        System.out.println("----------------------------\n");
    }

    /**
     * Override finalizeSale to record transaction in history for returns/exchanges
     */
    @Override
    public boolean finalizeSale(double paidAmount, Customer customer) {
        // Capture transaction details BEFORE calling super.finalizeSale() 
        // because super.finalizeSale() calls reset() which clears currentSale
        String transactionId = refundExchangeProcessor.getNextTransactionId();
        List<Item> itemsCopy = new ArrayList<>(currentSale);
        double totalAmount = total;
        
        boolean result = super.finalizeSale(paidAmount, customer);
        
        if (result) {
            // Record this transaction for potential future returns/exchanges
            Transaction transaction = new Transaction(
                transactionId,
                itemsCopy,
                totalAmount,
                paidAmount,
                changeReturned,
                System.currentTimeMillis(),
                lastPaymentMethod,  // Use the actual payment method
                customer
            );
            refundExchangeProcessor.recordTransaction(transaction);
            System.out.println("Transaction ID: " + transactionId);
        }
        
        return result;
    }

    // Gift Card purchase (optional)
    public GiftCard createGiftCard(String cardNumber, double amount) {
        GiftCard giftCard = new GiftCard(cardNumber);
        giftCard.loadAmount(amount);
        // Do not persist yet, save after payment completes
        this.pendingGiftCards.add(giftCard);
        return giftCard;
    }

    /**
     * Get the refund/exchange processor for this POS terminal
     */
    public RefundExchangeProcessor getRefundExchangeProcessor() {
        return refundExchangeProcessor;
    }

    /**
     * Set the payment method for this transaction
     */
    public void setPaymentMethod(String method) {
        this.lastPaymentMethod = method;
    }

    /**
     * Get the last payment method used
     */
    public String getLastPaymentMethod() {
        return lastPaymentMethod;
    }
}

