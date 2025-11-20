package StoreFloor;

import HR.BaseEmployee;
import Utils.Database;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPOSSystem implements POSComponent{
    protected List<Item> currentSale = new ArrayList<>();
    protected double total;

    // currently logged-in employee for this terminal
    protected BaseEmployee loggedInEmployee;

    @Override
    public void startTransaction(){
        currentSale.clear();
        total =0;
        System.out.println("Transaction Started");
    }

    @Override
    public void scanItem(Item item){
        currentSale.add(item);
        total += item.getPrice();
        System.out.println("Scanned "+ item.getName() + " Total: $"+ total);
    }

    @Override
    public boolean finalizeSale(PaymentMethod payment){
        System.out.println("Processing payment method:...");
        boolean success = payment.processPayment(total);
        if(success){
            printReceipt();
            reset();
        }
        return success;
    }

    @Override
    public boolean loginEmployee(String username, String password, String pin){
        // load employees from the exmployees.txt & find matching username
        Database db = new Database();
        for(BaseEmployee emp : db.getEmployees()){
            if(emp.getUsername() == null) continue;
            if(emp.getUsername().equals(username)){
                
                // verify password and pin
                if(!emp.verifyPassword(password)){
                    System.out.println("Invalid password for username: " + username);
                    return false;
                }
                if(!emp.verifyPin(pin)){
                    System.out.println("Invalid PIN for username: " + username);
                    return false;
                }
                if(this.loggedInEmployee != null){
                    System.out.println("Another employee is already logged in: " + this.loggedInEmployee.getFName());
                    return false;
                }
                this.loggedInEmployee = emp;
                System.out.println("Employee logged in: " + emp.getFName() + " " + emp.getLName());
                return true;
            }
        }
        System.out.println("No such username: " + username);
        return false;
    }

    @Override
    public void logoutEmployee(){
        if(this.loggedInEmployee != null){
            System.out.println("Employee logged out: " + this.loggedInEmployee.getFName() + " " + this.loggedInEmployee.getLName());
            this.loggedInEmployee = null;
        }
    }

    @Override
    public BaseEmployee getLoggedInEmployee(){
        return this.loggedInEmployee;
    }

    protected abstract void printReceipt();

    protected void reset(){
        currentSale.clear();
        total = 0;
    }
}
