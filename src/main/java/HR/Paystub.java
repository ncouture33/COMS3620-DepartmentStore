package HR;

public class Paystub {
    private final TimeCard card;
    private final double amount;
    private final String date;

    public Paystub(TimeCard card, double amount, String date) {
        this.card = card;
        this.amount = amount;
        this.date = date;
    }

    public TimeCard getCard() {
        return card;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}
