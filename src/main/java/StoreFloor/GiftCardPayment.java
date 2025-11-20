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
            return -1;
        }

        if (card.getBalance() < amount) {
            System.out.println("Gift card does not have enough balance.");
            return -1;
        }

        // Deduct the amount
        card.useAmount(amount);

        // Update database
        GiftCardDatabase.updateGiftCard(card);

        System.out.println("Gift card accepted! Remaining balance: $" + card.getBalance());

        return amount;
    }
}
