package HR;

public class Account {
    protected final String bank;
    protected final int routingNumber;
    protected final int accountNumber;

    public Account(String bank, int routingNumber, int accountNumber) {
        this.bank = bank;
        this.routingNumber = routingNumber;
        this.accountNumber = accountNumber;
    }

    public String getBank() {
        return bank;
    }

    public int getRoutingNumber() {
        return routingNumber;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public double transfer(double amount, Account account) {
        // In a real implementation, this would interact with banking systems.
        // Here, we simply return the amount deposited for demonstration purposes.
        return amount;
    }
}
