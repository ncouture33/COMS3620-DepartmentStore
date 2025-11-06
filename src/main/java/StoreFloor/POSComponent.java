package StoreFloor;

public interface POSComponent {
    void startTransaction();
    void scanItem(Item item);
    void applyAwards(Customer customer);
    boolean finalizeSale(PaymentMethod paymentMethod);
}
