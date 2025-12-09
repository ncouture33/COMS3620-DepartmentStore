package StoreFloor;

public class StorePOS extends AbstractPOSSystem {

    @Override
    public void applyAwards(Customer customer) {
        // if (customer.isRewardsMember()) {
        //     //give customer points
        //     //add discount logic here
        //     System.out.println("Applied rewards for customer: " + customer.getName());
        // } else {
        //     System.out.println("Customer is not a rewards member");
        // }
    }

    @Override
    protected void printReceipt() {
        System.out.println("\n--- RECEIPT ---");

        for (Item item : currentSale) {
            System.out.println(item.getName() + " - $" + String.format("%.2f", item.getPrice()));
        }

        System.out.println("----------------------------");

        System.out.println("Transaction Total: $" + String.format("%.2f", total));
        System.out.println("Total Paid:        $" + String.format("%.2f", totalPaid));

        if (changeReturned > 0) {
            System.out.println("Change Returned:   $" + String.format("%.2f", changeReturned));
        }

        System.out.println("----------------------------\n");
    }

    // Gift Card purchase (optional)
    public GiftCard createGiftCard(String cardNumber, double amount) {
        GiftCard giftCard = new GiftCard(cardNumber);
        giftCard.loadAmount(amount);
        GiftCardDatabase.saveGiftCard(giftCard);
        return giftCard;
    }
}
