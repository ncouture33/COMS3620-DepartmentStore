package HR;
import java.util.ArrayList;
import Utils.Data;

public class Payroll implements Data{

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

    public void setEmployees(ArrayList<BaseEmployee> employees){
        listEmployees = employees;
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

    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("Payroll Information:\n");
        str.append("Company Account: ").append(companyAccount.getBank()).append(" ").append(companyAccount.getRoutingNumber()).append(" ").append(companyAccount.getAccountNumber()).append("\n");
        str.append("Employees:\n");
        for(int i = 0; i < listEmployees.size(); i++){
            str.append(listEmployees.get(i).toString()).append("\n");
        }
        return str.toString();
    }

    @Override
    public String getData() {
        StringBuilder data = new StringBuilder();
        data.append("EMPLOYEES\n");
        for (int i = 0; i < listEmployees.size(); i++) {
            data.append(listEmployees.get(i).getData()).append("\n");
        }
        data.append("COMPANY_ACCOUNT\n");
        data.append(companyAccount.getData()).append("\n");
        return data.toString();
    }
}
