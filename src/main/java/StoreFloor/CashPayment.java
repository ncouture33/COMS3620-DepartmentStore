package StoreFloor;

public class CashPayment implements PaymentMethod {
    private double amountGiven;
    public CashPayment(double amountGiven){
        this.amountGiven = amountGiven;
    }
    @Override
    public double processPayment(double totalAmount) {
        if (amountGiven < totalAmount) {
            System.out.println("Partial cash provided: $" + String.format("%.2f", amountGiven));
            return amountGiven; // allow partial payment
        }

        double change = amountGiven - totalAmount;
        System.out.println("Change Due: $" + String.format("%.2f", change));

        return amountGiven; // total paid
    }
}
