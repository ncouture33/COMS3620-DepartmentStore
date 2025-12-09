package HR;

public class Salary extends BaseEmployee {
    private int salary;

    public Salary(int id, String fName, String lName, int DOB, int social, int salary, String department, String role) {
        super(id, fName, lName, DOB, social, department, role);
        this.salary = salary;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    @Override
    public double renderPayment(){
        //52 for 52 weeks of the year
        double percentageOfYear = card.getTimePeriod() / 52.0;
        return percentageOfYear * salary;
    }

    @Override
    public void resetTimeCard(){
        card = new TimeCard();
    }

    public String toString(){
        return "SALARY " + super.toString() + ", Salary: " + salary;
    }

    public String getData(){
        return "SALARY " + super.getData() + salary + " ";
    }
    @Override
    public void updateSalary(double newSalary){
        this.salary = (int)newSalary;
    }
}
