package StoreFloor;

public class GiftCard extends AbstractGiftCard {

    public GiftCard(String cardNumber) {
        super(cardNumber);
    }

    @Override
    public String getData() {
        return cardNumber + " " + this.getBalance();
    }

    // @Override
    // public String toString() {
    //     return "GiftCard{" +
    //             "cardNumber='" + cardNumber + '\'' +
    //             ", balance=" + this.getBalance() +
    //             '}';
    // }
    public void useAmount(double amount){
        double remainder = getBalance() - amount;
        if( remainder < 0){
            remainder =0;
        }
        this.setBalance( remainder);
    }
}
