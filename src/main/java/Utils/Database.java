package Utils;

import HR.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Database implements DatabaseWriter{

    @Override
    public void writeEmployee(BaseEmployee data) {
        try(FileWriter fwEmployee = new FileWriter("employees.txt", true)){
            fwEmployee.write(data.getData() + "\n");
            System.out.println("Successfully appended to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writePaystubs(ArrayList<Paystub> list, String date){
        String fileName = "paystubs-" + date + ".txt";
        try(FileWriter fwEmployee = new FileWriter("paystubs.txt", true)){
            for (Paystub data : list)
                fwEmployee.write(data.getData() + "\n");
            System.out.println("Successfully appended to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<BaseEmployee> getEmployees() {
        ArrayList<BaseEmployee> employees = new ArrayList<>();
        try (Scanner myReader = new Scanner(new File("employees.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                employees.add(parseEmployee(data));
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return employees;
    }

    private BaseEmployee parseEmployee(String data){
        //Parsing logic here
        BaseEmployee emp = null;
        TimeCard card = null;
        Scanner tempScanner = new Scanner(data);
        String empType = tempScanner.next();

        int id = tempScanner.nextInt();
        String fName = tempScanner.next();
        String lName = tempScanner.next();
        int DOB = tempScanner.nextInt();
        int social = tempScanner.nextInt();
        int timePeriod = tempScanner.nextInt();
        double hoursWorked = tempScanner.nextDouble();
        double overtimeHours = tempScanner.nextDouble();
        String bankName = tempScanner.next();
        int routingNum = tempScanner.nextInt();
        int accountNum = tempScanner.nextInt();
        Account account = new Account(bankName, routingNum, accountNum);
        if (empType.equals("SALARY")){
            int salary = tempScanner.nextInt();
            emp = new Salary(id, fName, lName, DOB, social, salary);
            emp.setAccount(account);
            card = new TimeCard(timePeriod, hoursWorked, overtimeHours);
            emp.setTimeCard(card);
            tempScanner.close();
            return emp;
        } else if (empType.equals("HOURLY")){
            double hourlyRate = tempScanner.nextDouble();
            double overtimeRate = tempScanner.nextDouble();
            emp = new Hourly(id, fName, lName, DOB, social, hourlyRate, overtimeRate);
            emp.setAccount(account);
            card = new TimeCard(timePeriod, hoursWorked, overtimeHours);
            emp.setTimeCard(card);
            tempScanner.close();
            return emp;
        }
        return null;
    }


    private Account parseAccount(String data){
        Scanner tempScanner = new Scanner(data);
        String bankName = tempScanner.next();
        int routingNum = tempScanner.nextInt();
        int accountNum = tempScanner.nextInt();
        tempScanner.close();
        return new Account(bankName, routingNum, accountNum);
    }

    @Override
    public void addPayroll() {

    }

    @Override
    public Payroll getPayroll() {
        ArrayList<BaseEmployee> employees = new ArrayList<>();
        Payroll payroll = null;
        try (Scanner myReader = new Scanner(new File("payroll.txt"))) {
            String line = myReader.nextLine();
            line = myReader.nextLine();
            while (!line.equals("COMPANY_ACCOUNT")) {
                employees.add(parseEmployee(line));
                line = myReader.nextLine();
            }
            line = myReader.nextLine();
            Account account = parseAccount(line);
            payroll = new Payroll(account);
            payroll.setEmployees(employees);
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return payroll;
    }
}
