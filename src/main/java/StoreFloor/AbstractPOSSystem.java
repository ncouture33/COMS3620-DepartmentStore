package StoreFloor;

import java.util.ArrayList;
import java.util.List;

import Utils.Database;
import Utils.DatabaseWriter;

public abstract class AbstractPOSSystem implements POSComponent{
    protected List<Item> currentSale = new ArrayList<>();
    protected double total;
    
    protected double totalPaid;
    protected double changeReturned;

    @Override
    public void startTransaction(){
        currentSale.clear();
        total =0;
        totalPaid = 0;
        changeReturned =0;
        System.out.println("Transaction Started");
    }
    @Override
    public void scanItem(Item item){
        currentSale.add(item);
        total += item.getPrice();
        System.out.println("Scanned "+ item.getName() + "\tTotal: $"+ total);
    }

    @Override
    public boolean finalizeSale(PaymentMethod payment, Customer customer) {
        System.out.println("Processing payment method...");

        // NEW: now returns amount paid
        double paid = payment.processPayment(total);

        if (paid < 0) {
            System.out.println("Payment failed.");
            return false;
        }

        totalPaid = paid;
        // earn 1 point for every $1 spent
        int pointsEarned = (int) paid; 
        if (customer != null && customer.isRewardsMember()) {
            customer.getRewards().addPoints(pointsEarned);
            System.out.println("Added " + pointsEarned + " points to customer " + customer.getName() + ". Total points: " + customer.getRewards().getPoints());
            DatabaseWriter database = new Database();
            database.updateCustomerRewardsPoints(customer.getRewards());
        }
        changeReturned = (paid > total) ? (paid - total) : 0;

        printReceipt();
        reset();
        return true;
    }
    protected abstract void printReceipt();

    protected void reset(){
        currentSale.clear();
        total = 0;total =0;
        totalPaid = 0;
        changeReturned =0; 
    }
}
