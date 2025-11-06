package StoreFloor;

public class CashPayment implements PaymentMethod {
    private double amountGiven;
    public CashPayment(double amountGiven){
        this.amountGiven = amountGiven;
    }
    @Override
    public boolean processPayment( double amount){
        if(amountGiven >= amount){
            return true;
        }
        return false;
    }
}
