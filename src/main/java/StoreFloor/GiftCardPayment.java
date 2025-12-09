package StoreFloor;

public class GiftCardPayment implements PaymentMethod {

    private String cardNumber;

    public GiftCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public double processPayment(double amount) {

        GiftCard card = GiftCardDatabase.loadGiftCard(cardNumber);

        if (card == null) {
            System.out.println("Gift card not found!");
            return 0;
        }
        double available = card.getBalance();
        double toUse = Math.min(available, amount);
        if (toUse <= 0) {
            System.out.println("Gift card has no balance.");
            return 0;
        }

        // Deduct the available amount (partial or full)
        card.useAmount(toUse);

        // Update database with new balance
        GiftCardDatabase.updateGiftCard(card);

        System.out.println("Gift card applied: $" + String.format("%.2f", toUse) + ". Remaining balance: $" + String.format("%.2f", card.getBalance()));

        return toUse;
    }
}
