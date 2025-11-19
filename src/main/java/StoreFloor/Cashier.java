package StoreFloor;

import HR.Hourly;

public class Cashier extends Hourly{
   public Cashier(int id, String fName, String lName, int DOB, int social, double hourlyRate, double overtimeRate) {
        super(id, fName, lName, DOB, social, hourlyRate, overtimeRate);
    }

    public void ringUpItem( Item item, POSComponent posSystem){
        posSystem.scanItem(item);
    }
    public void applyAwards(Customer customer, POSComponent posSystem){
        posSystem.applyAwards(customer);
    }
    public void completeSale(POSComponent posSystem, PaymentMethod payment){
        posSystem.finalizeSale(payment);
    }
    public GiftCard processGiftCard(StorePOS posSystem, String cardNumber, double amount){
        return posSystem.createGiftCard(cardNumber, amount);
    }
}