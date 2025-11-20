package StoreFloor;

public class CashPayment implements PaymentMethod {
    private double amountGiven;
    public CashPayment(double amountGiven){
        this.amountGiven = amountGiven;
    }
    @Override
    public double processPayment(double totalAmount) {
        if (amountGiven < totalAmount) {
            System.out.println("Not enough cash provided.");
            return -1; // payment failed
        }

        double change = amountGiven - totalAmount;
        System.out.println("Change Due: $" + String.format("%.2f", change));

        return amountGiven; // total paid
    }
}
