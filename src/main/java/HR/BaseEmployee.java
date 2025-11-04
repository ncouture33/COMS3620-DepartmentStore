package HR;

public abstract class BaseEmployee implements EmployeeActions{
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

}
