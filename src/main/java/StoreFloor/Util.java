package StoreFloor;

import java.util.Scanner;

import Utils.Database;
import Utils.DatabaseWriter;

public class Util {
    public static void runSales(Scanner scanner) {
        System.out.println("\n--- Point of Sale ---");
        DatabaseWriter database = new Database();

        StorePOS pos = new StorePOS();
        Cashier cashier = new Cashier(0,"John", "Doe", 03,1234567890,12.0,18);

        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();

        System.out.print("Is the customer a rewards member? (yes/no): ");
        boolean member = scanner.nextLine().equalsIgnoreCase("yes");
        Customer customer = null;

        if (member){
            System.out.print("Enter phone number: ");
            String phoneNumber = scanner.nextLine();
            
            Rewards rewards = database.getCustomerRewards(phoneNumber);
            if (rewards == null){
                System.out.println("No rewards account found for phone number " + phoneNumber);
            }
            else{
                customer = new Customer(name, true);
                customer.setRewards(rewards);
                System.out.println("Rewards ID " + rewards.getId() + " linked to customer " + name + " with phone number " + phoneNumber);
            }  
        }
        else{
            System.out.print("Would you like to join the rewards program? (yes/no): ");
            boolean answer = scanner.nextLine().equalsIgnoreCase("yes");
            if (answer){
                //add the customer to the rewards program
                System.out.print("Enter phone number: ");
                String phoneNumber = scanner.nextLine();
                System.out.print("Enter email: ");
                String email = scanner.nextLine();
                customer = new Customer(name, true);
                Rewards rewards = new Rewards(-1, 0, email, phoneNumber);
                int id = database.generateCustomerRewardsID();
                rewards.setID(id);
                customer.setRewards(rewards);
                database.addCustomerToRewardsProgram(customer);
                System.out.println("Customer " + name + " added to rewards program with phone number " + phoneNumber);
            }
        }

        

        pos.startTransaction();

        while (true) {
            System.out.print("Enter item name (or 'done' to finish, or 'giftcard' to buy a gift card): ");
            String itemName = scanner.nextLine();
            if (itemName.equalsIgnoreCase("done"))
                break;
        
            double price = 0;
        
            if (itemName.equalsIgnoreCase("giftcard")) {
                System.out.print("Enter gift card number: ");
                String cardNumber = scanner.nextLine();
                System.out.print("Enter gift card amount: ");
                price = Double.parseDouble(scanner.nextLine());
                GiftCard giftCard = new GiftCard(cardNumber);
                giftCard.loadAmount(price);
                Item giftCardItem = new Item("Gift Card", price);
                cashier.ringUpItem(giftCardItem, pos);
                System.out.println(giftCard.toString());
                continue;
            } else {
                System.out.print("Enter price: ");
                price = Double.parseDouble(scanner.nextLine());
                Item item = new Item(itemName, price);
                cashier.ringUpItem(item, pos);
            }
        }
        
        System.out.print("Pay with (cash/card): ");
        String method = scanner.nextLine();

        if (method.equalsIgnoreCase("cash")) {
            System.out.print("Enter cash amount: ");
            double cash = Double.parseDouble(scanner.nextLine());
            PaymentMethod payment = new CashPayment(cash);
            cashier.completeSale(pos, payment, customer);
        } else {
            PaymentMethod payment = new CardPayment();
            cashier.completeSale(pos, payment, customer);
        }

        System.out.println("Transaction complete.\n");
    }
}