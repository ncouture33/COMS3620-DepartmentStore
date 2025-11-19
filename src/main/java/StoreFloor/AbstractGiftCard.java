package StoreFloor;

public abstract class AbstractGiftCard implements giftCardLoadable{
    protected String cardNumber;
    protected double balance;

    public AbstractGiftCard(String cardNumber){
        this.cardNumber = cardNumber;
    }
    @Override
    public void loadAmount(double amount){
        if(amount > 0){
            balance += amount;
            System.out.println("Loaded $"+ amount + " to Gift Card "+ cardNumber + ". New Balance: $"+ balance);
        } else {
            System.out.println("Invalid amount to load: $"+ amount);
        }
    }
    public String getCardNumber()
    {
        return cardNumber;
    }
    public double getBalance()
    {
        return balance;
    }
}
