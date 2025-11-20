package Utils;

import HR.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Database implements DatabaseWriter{

    @Override
    public void writeEmployee(BaseEmployee data) {
        try(FileWriter fwEmployee = new FileWriter("employees.txt", true)){
            fwEmployee.write(data.getData() + "\n");
            System.out.println("Successfully appended to the file.");
            writeRegisterCredentials(data);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Append credentials for an employee to registerEmployees.txt in the format:
     * id username salt hash pin
     */
    public void writeRegisterCredentials(BaseEmployee data){
        File credFile = new File("registerEmployees.txt");
        java.util.ArrayList<String> keep = new java.util.ArrayList<>();
        String newUsername = data.getUsername() == null ? "" : data.getUsername();
        int newId = data.getID();

        // read existing lines and keep those that do not belong to this employee id or username
        if (credFile.exists()){
            try (Scanner reader = new Scanner(credFile)){
                while (reader.hasNextLine()){
                    String line = reader.nextLine().trim();
                    if(line.isEmpty()) continue;
                    try (Scanner s = new Scanner(line)){
                        int id = s.nextInt();
                        String username = s.hasNext() ? s.next() : "";
                        // skip entries matching this id or username
                        if(id == newId) continue;
                        if(!username.isEmpty() && username.equals(newUsername)) continue;
                        keep.add(line);
                    } catch (Exception ex){
                        
                        // if malformed, keep the line to avoid data loss
                        keep.add(line);
                    }
                }
            } 
            catch (Exception e){
                // if reading fails, proceed to overwrite by appending only the new entry
            }
        }

        // prepare new line for this employee
        String username = data.getUsername() == null ? "" : data.getUsername();
        String salt = data.getStoredPasswordSalt() == null ? "" : data.getStoredPasswordSalt();
        String hash = data.getStoredPasswordHash() == null ? "" : data.getStoredPasswordHash();
        String pin = data.getPin() == null ? "" : data.getPin();
        String newLine = data.getID() + " " + username + " " + salt + " " + hash + " " + pin;

        // write back kept lines + the new line
        try (FileWriter fw = new FileWriter(credFile, false)){
            for (String l : keep) fw.write(l + "\n");
            fw.write(newLine + "\n");
            System.out.println("Successfully wrote credentials to registerEmployees.txt");
        } catch (IOException e) {
            System.out.println("An error occurred writing credentials.");
            e.printStackTrace();
        }
    }

    @Override
    public void indicateEmployeeOffboarding(int empId, String date, String reasonForLeaving) {
        try (Scanner myReader = new Scanner(new File("employees.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                BaseEmployee emp = parseEmployee(data);
                if(emp.getID() == empId){
                    try(FileWriter fwEmployee = new FileWriter("employeesInOffboardingProcess.txt", true)){
                        OffboardingEmployee offemp = new OffboardingEmployee(empId, emp.getFName(), emp.getLName(), emp.getDOB(), emp.getSocial(), date, reasonForLeaving);
                        fwEmployee.write(offemp.getData() + "\n");
                        System.out.println("Successfully appended to the offboarding file.");
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } 
    }

    @Override
    public void writePriorEmployee(OffboardingEmployee emp, String propertyReturned){
        try(FileWriter fwEmployee = new FileWriter("priorEmployees.txt", true)){
            fwEmployee.write(emp.getData() + " " + propertyReturned + "\n");
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
        
        // Attempt to load stored credentials from registerEmployees.txt and attach them to parsed employees
        java.util.ArrayList<String[]> creds = new java.util.ArrayList<>();
        try (Scanner credReader = new Scanner(new File("registerEmployees.txt"))) {
            while (credReader.hasNextLine()) {
                String line = credReader.nextLine().trim();
                
                if (line.isEmpty()) continue;
                
                try (Scanner s = new Scanner(line)) {
                    int id = s.nextInt();
                    String username = s.hasNext() ? s.next() : "";
                    String salt = s.hasNext() ? s.next() : "";
                    String hash = s.hasNext() ? s.next() : "";
                    String pin = s.hasNext() ? s.next() : "";
                    creds.add(new String[]{String.valueOf(id), username, salt, hash, pin});
                } catch (Exception ex) {
                    // skip malformed credential lines
                }
            }
        } catch (Exception e) {
            // no credentials file yet
        }

        // attach credentials to employees (search list for matching id)
        for (BaseEmployee emp : employees) {
            String[] c = null;
            for (String[] entry : creds) {
                try {
                    if (Integer.parseInt(entry[0]) == emp.getID()) {
                        c = entry;
                        break;
                    }
                } catch (Exception ex) {
                    // ignore malformed id
                }
            }
            if (c != null) {
                if (c.length > 1 && c[1] != null && !c[1].isEmpty()) emp.setUsername(c[1]);
                if (c.length > 3 && c[2] != null && !c[2].isEmpty() && c[3] != null && !c[3].isEmpty()) emp.setStoredPassword(c[2], c[3]);
                if (c.length > 4 && c[4] != null && !c[4].isEmpty()) emp.setPin(c[4]);
            }
        }

        return employees;
    }
    @Override
    public OffboardingEmployee getOffboardingEmployee(int empID) {
        try (Scanner myReader = new Scanner(new File("employeesInOffboardingProcess.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                OffboardingEmployee emp = parseOffBoardEmployee(data);
                if(emp.getID() == empID){
                    return emp;
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } 
        return null;
    }
@Override
public void removeOffboardingEmployee(int empID) {
    File inputFile = new File("employeesInOffboardingProcess.txt");
    File tempFile = new File("temp.txt");

    try (
        Scanner myReader = new Scanner(inputFile);
        PrintWriter writer = new PrintWriter(tempFile);
    ) {
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            OffboardingEmployee emp = parseOffBoardEmployee(data);

            // Only keep employees whose ID doesn't match
            if (emp.getID() != empID) {
                writer.println(data);
            }
        }
    } catch (Exception e) {
        System.out.println("An error occurred while removing employee.");
        e.printStackTrace();
        return;
    }

    // Replace original file with the new one
    if (!inputFile.delete()) {
        System.out.println("Could not delete original file.");
        return;
    }
    if (!tempFile.renameTo(inputFile)) {
        System.out.println("Could not rename temp file.");
    }
}
@Override
public void removeFromEmployee(int empID){
    File inputFile = new File("employees.txt");
    File tempFile = new File("temp.txt");

    try (
        Scanner myReader = new Scanner(inputFile);
        PrintWriter writer = new PrintWriter(tempFile);
    ) {
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine().trim();
            if (data.isEmpty()) continue;

            BaseEmployee emp;
            try {
                emp = parseEmployee(data); //  parseEmployee for employees.txt
            } catch (Exception e) {
                System.out.println("Skipping malformed line: " + data);
                continue;
            }

            // keep employees whose ID don't match
            if (emp.getID() != empID) {
                writer.println(data);
            }
        }
    } catch (Exception e) {
        System.out.println("An error occurred while removing employee.");
        e.printStackTrace();
        return;
    }

    // Replace original file with temp
    if (!inputFile.delete()) {
        System.out.println("Could not delete original file");
        return;
    }
    if (!tempFile.renameTo(inputFile)) {
        System.out.println("Could not rename temp file");
    }
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

    private OffboardingEmployee parseOffBoardEmployee(String data){
        //Parsing logic here
        Scanner tempScanner = new Scanner(data);
        int id = tempScanner.nextInt();
        String fName = tempScanner.next();
        String lName = tempScanner.next();
        int DOB = tempScanner.nextInt();
        int social = tempScanner.nextInt();
        String date = tempScanner.next();
        String reasonForLeaving = "";
        if (tempScanner.hasNextLine()) {
            reasonForLeaving = tempScanner.nextLine().trim();
        }
        

        OffboardingEmployee emp = new OffboardingEmployee(id, fName, lName, DOB, social, date, reasonForLeaving);
        tempScanner.close();
        return emp;
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
