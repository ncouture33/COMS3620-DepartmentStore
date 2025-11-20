package Utils;

import HR.*;
import StoreOperations.ClockTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Database implements DatabaseWriter {

    @Override
    public void writeEmployee(BaseEmployee data) {
        try (FileWriter fwEmployee = new FileWriter("employees.txt", true)) {
            fwEmployee.write(data.getData() + "\n");
            System.out.println("Successfully appended to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public void indicateEmployeeOffboarding(int empId, String date, String reasonForLeaving) {
        try (Scanner myReader = new Scanner(new File("employees.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                BaseEmployee emp = parseEmployee(data);
                if (emp.getID() == empId) {
                    try (FileWriter fwEmployee = new FileWriter("employeesInOffboardingProcess.txt", true)) {
                        OffboardingEmployee offemp = new OffboardingEmployee(empId, emp.getFName(), emp.getLName(),
                                emp.getDOB(), emp.getSocial(), date, reasonForLeaving);
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
    public void writePriorEmployee(OffboardingEmployee emp, String propertyReturned) {
        try (FileWriter fwEmployee = new FileWriter("priorEmployees.txt", true)) {
            fwEmployee.write(emp.getData() + " " + propertyReturned + "\n");
            System.out.println("Successfully appended to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writePaystubs(ArrayList<Paystub> list, String date) {
        String fileName = "paystubs-" + date + ".txt";
        try (FileWriter fwEmployee = new FileWriter("paystubs.txt", true)) {
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

    @Override
    public ArrayList<ClockTime> getClockedEmployees() {
        ArrayList<ClockTime> clockedEmployees = new ArrayList<>();
        try (Scanner myReader = new Scanner(new File("clockedInEmployees.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                clockedEmployees.add(parseClockTime(data));
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return clockedEmployees;
    }

    private ClockTime parseClockTime(String data) {
        Scanner tempScanner = new Scanner(data);
        int id = tempScanner.nextInt();
        int date = tempScanner.nextInt();
        String clockInTime = tempScanner.next();
        ClockTime myClockTime = new ClockTime(id, date, clockInTime);
        tempScanner.close();
        return myClockTime;
    }

    @Override
    public OffboardingEmployee getOffboardingEmployee(int empID) {
        try (Scanner myReader = new Scanner(new File("employeesInOffboardingProcess.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                OffboardingEmployee emp = parseOffBoardEmployee(data);
                if (emp.getID() == empID) {
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
                PrintWriter writer = new PrintWriter(tempFile);) {
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
    public void removeFromEmployee(int empID) {
        File inputFile = new File("employees.txt");
        File tempFile = new File("temp.txt");

        try (
                Scanner myReader = new Scanner(inputFile);
                PrintWriter writer = new PrintWriter(tempFile);) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().trim();
                if (data.isEmpty())
                    continue;

                BaseEmployee emp;
                try {
                    emp = parseEmployee(data); // parseEmployee for employees.txt
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

    private BaseEmployee parseEmployee(String data) {
        // Parsing logic here
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
        if (empType.equals("SALARY")) {
            int salary = tempScanner.nextInt();
            emp = new Salary(id, fName, lName, DOB, social, salary);
            emp.setAccount(account);
            card = new TimeCard(timePeriod, hoursWorked, overtimeHours);
            emp.setTimeCard(card);
            tempScanner.close();
            return emp;
        } else if (empType.equals("HOURLY")) {
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

    private OffboardingEmployee parseOffBoardEmployee(String data) {
        // Parsing logic here
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

    private Account parseAccount(String data) {
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

    @Override
    public void writeClockedInEmployee(BaseEmployee emp, String time, int date) {
        try (FileWriter fwClockedIn = new FileWriter("clockedInEmployees.txt", true)) {
            fwClockedIn.write(emp.getSocial() + " " + date + " " + time + "\n");
            System.out.println("Successfully appended to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public void clockOutEmployee(ClockTime emp) {
        File inputFile = new File("clockedInEmployees.txt");
        File tempFile = new File("clockedInEmployees_temp.txt");

        String socialString = String.valueOf(emp.getSocial());
        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            String currentLine;
            boolean removed = false;

            while ((currentLine = reader.readLine()) != null) {
                // If the line begins with this employee's social ID, remove it
                if (currentLine.startsWith(socialString + " ")) {
                    removed = true;
                    continue; // removes this line
                }
                writer.println(currentLine);
            }
            writer.flush();
            // Replace original file with updated one
            if (!inputFile.delete()) {
                System.out.println("Could not delete original file!");
            }
            if (!tempFile.renameTo(inputFile)) {
                System.out.println("Could not rename temp file");
            }
            // need to implement updating time card + writing to all time history

            if (removed) {
                System.out.println("Employee clocked out");
            } else {
                System.out.println("Employee was not clocked in.");
            }

        } catch (IOException e) {
            System.out.println("An error occurred while clocking out.");
            e.printStackTrace();
        }
    }

    @Override
    public void writeTimeHistory(ClockTime emp) {
        try (FileWriter fwTimeHistory = new FileWriter("employeeTimeHistory.txt", true)) {
            fwTimeHistory.write(emp.getData() + "\n");
            System.out.println("Successfully appended to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
