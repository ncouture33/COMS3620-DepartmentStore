package HR;

import Utils.Data;

public class OffboardingEmployee extends BaseEmployee implements EmployeeActions, Data{

    protected String reasonOfLeaving;
    protected String date;

    public OffboardingEmployee(int id, String fName, String lName, int DOB, int social, String date,String reasonOfLeaving) {
        super(id, fName, lName, DOB, social); 
        this.reasonOfLeaving = reasonOfLeaving;
        this.date = date;
    }
    public String getReasonOfLeaving() {
        return reasonOfLeaving;
    }


    public void setReasonOfLeaving(String reasonOfLeaving) {
        this.reasonOfLeaving = reasonOfLeaving;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    @Override
    public String toString(){
        return "ID: " + id + ", Name: " + fName + " " + lName + ", DOB: " + DOB + ", Social: " + social +  ", Departure Date: "+ date + " " +", Reason of Leaving: [" + reasonOfLeaving+ "]" + " ";
    }

    @Override
    public String getData(){
        return id + " " + fName + " " + lName +  " " + DOB + " " + social + " " + date + " "+ reasonOfLeaving;
    }
    @Override
    public double renderPayment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renderPayment'");
    }

    @Override
    public void resetTimeCard() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetTimeCard'");
    }
    
}
