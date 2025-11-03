public class Payroll {

    private ArrayList<Employees> listEmployees;
    // private ArrayList<Timecard> listCards;

    public Payroll(){
        list = new ArrayList<Employees>();
    }

    public boolean addEmployee(Employee employee){
        listEmployees.add(employee);
    }

    public boolean removeEmployee(Employee employee){
        listEmployees.remove(employee);
    }   

    public ArrayList<Paycheck> payEmployees(int date){
        ArrayList<Paycheck> checks = new ArrayList<Paycheck>();
        for(int i = 0; i < list.length(); i++){
            checks.add(payEmployee(list.get(i)));
        }
    }

    private Paycheck payEmployee(Employee employee){
        Account accountToDepositTo = employee.getAccount();
        Paycheck payCheck = accountToDepositTo.deposit(Timecard card);
        return payCheck;
    }

}