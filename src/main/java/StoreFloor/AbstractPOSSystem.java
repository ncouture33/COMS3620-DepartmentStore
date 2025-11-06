package StoreFloor;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPOSSystem implements POSComponent{
    protected List<Item> currentSale = new ArrayList<>();
    protected double total;

    @Override
    public void startTransaction(){
        currentSale.clear();
        total =0;
        System.out.println("Transaction Started");
    }
    @Override
    public void scanItem(Item item){
        currentSale.add(item);
        total += item.getPrice();
        System.out.println("Scanned "+ item.getName() + "Total: $"+ total);
    }
    @Override
    public boolean finalizeSale(PaymentMethod payment){
        System.out.println("Processing payment method:...");
        boolean success = payment.processPayment(total);
        if(success){
            printReceipt();
            reset();
        }
        return success;
    }
    protected abstract void printReceipt();

    protected void reset(){
        currentSale.clear();
        total = 0;
    }
}
