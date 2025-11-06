package HR;

import Utils.Data;
import java.io.File;
import java.util.Scanner;

public abstract class BaseEmployee implements EmployeeActions, Data {
    protected int id;
    protected String fName;
    protected String lName;
    protected int DOB;
    protected int social;
    protected Account directDepositAccount;
    protected TimeCard card;

    public BaseEmployee(int id, String fName, String lName, int DOB, int social) {
        this.card = new TimeCard();
        this.id = id;
        this.fName = fName;
        this.lName = lName;
        this.DOB = DOB;
        this.social = social;
        this.card = new TimeCard();
    }

    public int getID(){
        return id;
    }

    public void setID(int id){
        this.id = id;
    }

    public Account getAccount() {
        return directDepositAccount;
    }


    public void setAccount(Account account) {
        directDepositAccount = account;
    }


    public String getFName() {
        return fName;
    }


    public void setFName(String fname) {
        this.fName = fname;
    }


    public String getLName() {
        return lName;
    }


    public void setLName(String lname) {
        this.lName = lname;
    }


    public int getDOB() {
        return DOB;
    }


    public void setDOB(int dob) {
        this.DOB = dob;
    }


    public int getSocial() {
        return social;
    }


    public void setSocial(int social) {
        this.social = social;
    }
    

    public void setTimeCard(TimeCard card) {
        this.card = card;
    }


    public TimeCard getTimecard() {
        return card;
    }


    public static synchronized int getNextEmployeeID(){
        
        int nextEmployeeID = 1;

        try (Scanner reader = new Scanner(new File("employees.txt"))) {
            int maxId = 0;
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;
                try (Scanner lineScanner = new Scanner(line)) {
                    if (!lineScanner.hasNext()) continue;
                    lineScanner.next();
                    if (lineScanner.hasNextInt()) {
                        int id = lineScanner.nextInt();
                        if (id > maxId) maxId = id;
                    }
                } catch (Exception e) {

                }
            }
            nextEmployeeID = Math.max(1, maxId + 1);
        } catch (Exception e) {
            // File not found or unreadable
            nextEmployeeID = 1;
        }
        
        return nextEmployeeID++;
    }

    public String toString(){
        return "ID: " + id + ", Name: " + fName + " " + lName + ", DOB: " + DOB + ", Social: " + social + ", TimeCard: [" + card.toString() + "]";
    }

    public String getData(){
        return id + " " + fName + " " + lName +  " " + DOB + " " + social + " " + card.getData() + " " + directDepositAccount.getData() + " ";
    }

}
