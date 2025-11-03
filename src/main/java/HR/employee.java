public class Employee{
    private int id;
    private String fName;
    private String lName;
    private int DOB;
    private int social;
    private Account directDepositAccount;

    public Account getAccount(){
        return directDepositAccount;
    }
}