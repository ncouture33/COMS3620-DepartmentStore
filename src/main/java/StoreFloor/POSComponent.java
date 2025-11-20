package StoreFloor;

import HR.BaseEmployee;

public interface POSComponent {
    // Transaction APIs
    void startTransaction();
    void scanItem(Item item);
    void applyAwards(Customer customer);
    boolean finalizeSale(PaymentMethod paymentMethod, Customer customer);

    // Authentication
    boolean loginEmployee(String username, String password, String pin);
    void logoutEmployee();
    BaseEmployee getLoggedInEmployee();
}
