package StoreFloor;

import HR.Hourly;

public class Cashier {
    Hourly cashier = new Hourly(0, null, null, 0, 0, 0, 0);

    public void ringUpItem( Item item, POSComponent posSystem){
        posSystem.scanItem(item);
    }
    public void applyAwards(Customer customer, POSComponent posSystem){
        posSystem.applyAwards(customer);
    }
    public void completeSale(POSComponent posSystem, PaymentMethod payment){
        posSystem.finalizeSale(payment);
    }
}