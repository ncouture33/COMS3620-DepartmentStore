package StoreFloor;

public class CashPayment implements PaymentMethod {
    private double amountGiven;
    public CashPayment(double amountGiven){
        this.amountGiven = amountGiven;
    }
    @Override
    public boolean processPayment( double amount){
        double change = amountGiven - amount;
        System.out.println("Change Due: $" + String.format("%.2f", change));
        if(amountGiven >= amount){
            return true;
        }
        return false;
    }
}
