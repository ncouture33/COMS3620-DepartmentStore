package StoreFloor;



public class StorePOS extends AbstractPOSSystem{
    @Override
    public void applyAwards(Customer customer){
        if(customer.isRewardsMember()){
            // DO SOMETHING 
        }else{
            System.out.println("Customer is not a rewards member");
        }
    }
    @Override
    protected void printReceipt(){
        System.out.println("\n--- RECEIPT---");
        for(Item item : currentSale){
            System.out.println(item.getName());
        }
        System.out.println("Total Paid: $" + total);

    }
    // Gift Card Functionality
    public GiftCard createGiftCard(String cardNumber, double amount){
        GiftCard giftCard = new GiftCard(cardNumber);
        giftCard.loadAmount(amount);
        total += amount;
        return giftCard;
    }
    
}
