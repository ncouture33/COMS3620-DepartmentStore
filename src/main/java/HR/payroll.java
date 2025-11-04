package HR;
import java.util.ArrayList;

public class Payroll {

    private ArrayList<BaseEmployee> listEmployees;
    private Account companyAccount;

    public Payroll(Account companyAccount){
        listEmployees = new ArrayList<BaseEmployee>();
        this.companyAccount = companyAccount;
    }

    public void addEmployee(BaseEmployee employee){
        listEmployees.add(employee);
    }

    public void removeEmployee(BaseEmployee employee){
        listEmployees.remove(employee);
    }   

    public ArrayList<Paystub> payEmployees(String date){
        ArrayList<Paystub> checks = new ArrayList<Paystub>();
        for(int i = 0; i < listEmployees.size(); i++){
            double amount = payEmployee(listEmployees.get(i));
            Paystub paystub = producePaystub(amount, listEmployees.get(i).getTimecard(), date);
            checks.add(paystub);
        }
        return checks;
    }

    private double payEmployee(BaseEmployee employee){
        return companyAccount.transfer(employee.renderPayment(), employee.getAccount());
    }

    private Paystub producePaystub(double amount, TimeCard card, String date){
        return new Paystub(card, amount, date);
    }

}
