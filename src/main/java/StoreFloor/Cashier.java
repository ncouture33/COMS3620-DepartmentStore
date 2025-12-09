package StoreFloor;

import HR.Hourly;
import java.util.Scanner;

public class Cashier extends Hourly{
   public Cashier(int id, String fName, String lName, int DOB, int social, double hourlyRate, double overtimeRate) {
        super(id, fName, lName, DOB, social, hourlyRate, overtimeRate);
    }

    public void ringUpItem( Item item, POSComponent posSystem){
        posSystem.scanItem(item);
    }
    public void applyAwards(Customer customer, POSComponent posSystem){
        posSystem.applyAwards(customer);
    }
    public void completeSale(POSComponent posSystem, PaymentMethod payment, Customer customer){
        posSystem.finalizeSale(payment, customer);
    }
    public GiftCard processGiftCard(StorePOS posSystem, String cardNumber, double amount){
        return posSystem.createGiftCard(cardNumber, amount);
    }

    // helpers for authentication using stored username and provided password & PIN
    public boolean signInToPOS(POSComponent pos, String password, String pin){
        if(pos == null) return false;
        String username = this.getUsername();
        if(username == null) return false;
        return pos.loginEmployee(username, password, pin);
    }

    public void signOutOfPOS(POSComponent pos){
        if(pos == null) return;
        if(pos.getLoggedInEmployee() == this){
            pos.logoutEmployee();
        } else {
            System.out.println("This cashier is not the one currently logged in on that POS.");
        }
    }

    /**
     * Initiates a return/refund process for a customer
     * Use Case #19: Main Success Scenario
     */
    public boolean processReturn(Customer customer, RefundExchangeProcessor processor, Scanner input) {
        System.out.print("Enter the transaction ID from the receipt: ");
        String transactionId = input.nextLine();
        
        return processor.processReturnExchange(this, customer, transactionId, input);
    }

    /**
     * Initiates an exchange process for a customer
     * Use Case #19: Alternate Flow
     */
    public boolean processExchange(Customer customer, RefundExchangeProcessor processor, 
                                   Scanner input, StorePOS posSystem) {
        System.out.print("Enter the transaction ID from the receipt: ");
        String transactionId = input.nextLine();
        
        return processor.processExchange(this, customer, transactionId, input, posSystem);
    }
}
