package StoreFloor;

import java.util.ArrayList;
import java.util.List;

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
        System.out.println("Scanned "+ item.getName() + "Total: $"+ total);
    }
    @Override
    public boolean finalizeSale(PaymentMethod payment) {
        System.out.println("Processing payment method...");

        // NEW: now returns amount paid
        double paid = payment.processPayment(total);

        if (paid < 0) {
            System.out.println("Payment failed.");
            return false;
        }

        totalPaid = paid;
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
