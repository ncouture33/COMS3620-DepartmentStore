package StoreFloor;

public class CardPayment implements PaymentMethod {
    // can add more to this method to check if card balance has enough money
    @Override
    public boolean processPayment(double amount){
        System.out.println("Process ... Approved");
        return true;
    }
}
