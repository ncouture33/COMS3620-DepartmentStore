package StoreFloor;

public class GiftCard extends AbstractGiftCard {
    public GiftCard(String cardNumber) {
        super(cardNumber);
    }
    @Override
    public String toString() {
        return "GiftCard{" +
                "cardNumber='" + cardNumber + '\'' +
                ", balance=" + this.getBalance() +
                '}';
    }
    
}
