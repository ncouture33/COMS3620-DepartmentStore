package HR;

import java.util.ArrayList;

public class Util {
    public static void payEmployees(){
        System.out.println("Payroll is being executed..");

        Account account = new Account("Von Maur", 123456789, 987654321);
        Payroll payroll = new Payroll(account);
        BaseEmployee emp1 = new Salary(1, "John", "Doe", 19900101, 123456789, 52000);
        BaseEmployee emp2 = new Hourly(2, "Jane", "Smith", 19920202, 987654321, 20.0, 30.0);
        emp1.setAccount(new Account("Bank of America", 111111111, 222222222));
        emp2.setAccount(new Account("Chase", 333333333, 444444444));

        emp1.getTimecard().increaseHoursWorked(35);
        emp1.getTimecard().setTimePeriod(2);
        emp2.getTimecard().increaseHoursWorked(45);
        emp2.getTimecard().setTimePeriod(2);

        payroll.addEmployee(emp1);
        payroll.addEmployee(emp2);

        //Payday: :)
        String date = "11/3/2025";
        ArrayList<Paystub> list = payroll.payEmployees(date);
        for (int i = 0; i < list.size(); i++){
            Paystub stub = list.get(i);
            TimeCard card = stub.getCard();
            System.out.println("Card: " + card + "Amount: " + stub.getAmount() + " Date: " + stub.getDate());
        }

        emp1.resetTimeCard();
        emp2.resetTimeCard();
    }
}
