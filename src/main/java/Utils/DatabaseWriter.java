package Utils;

import HR.BaseEmployee;
import HR.Payroll;
import HR.Paystub;

import java.util.ArrayList;

public interface DatabaseWriter {
    public void writeEmployee(BaseEmployee data);

    public ArrayList<BaseEmployee> getEmployees();

    public void addPayroll();

    public Payroll getPayroll();

    public void writePaystubs(ArrayList<Paystub> list, String date);
}
