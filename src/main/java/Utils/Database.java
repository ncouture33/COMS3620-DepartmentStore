package Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import HR.Account;
import HR.BaseEmployee;
import HR.Hourly;
import HR.OffboardingEmployee;
import HR.Payroll;
import HR.Paystub;
import HR.Salary;
import HR.Schedule;
import HR.Shift;
import HR.TimeCard;
import HR.Timeoff;
import StoreFloor.Customer;
import StoreFloor.Rewards;

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

    public ArrayList<Schedule> getSchedules() {
        ArrayList<Schedule> schedules = new ArrayList<>();
        try (Scanner myReader = new Scanner(new File("schedules.txt"))) {
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine().trim();
                if (line.isEmpty()) continue; // skip blank separators

                Scanner tempScanner = new Scanner(line);
                int scheduleId = tempScanner.nextInt();
                int hoursOpen = tempScanner.nextInt();
                int shiftLength = tempScanner.nextInt();
                int minStaff = tempScanner.nextInt();
                tempScanner.close();

                Schedule schedule = new Schedule(scheduleId, hoursOpen, shiftLength, minStaff);

                // Read following shift lines until ENDSHIFTS marker
                while (myReader.hasNextLine()) {
                    String nextLine = myReader.nextLine().trim();
                    if (nextLine.isEmpty()) continue; // ignore stray blanks
                    if (nextLine.equals("ENDSHIFTS")) break;

                    Scanner shiftScanner = new Scanner(nextLine);
                    int shiftId = shiftScanner.nextInt();
                    String day = shiftScanner.next();
                    String shiftStart = shiftScanner.next();
                    String shiftEnd = shiftScanner.next();
                    shiftScanner.close();

                    Shift shift = new Shift(shiftId, day, shiftStart, shiftEnd);
                    schedule.addShift(shift);
                }

                schedules.add(schedule);
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return schedules;
    }
    public int getNextScheduleID(){
        int maxId = 0;
        try {
            ArrayList<Schedule> scheds = getSchedules();
            for(Schedule sched : scheds){
                if(sched.getScheduleId() > maxId){
                    maxId = sched.getScheduleId();
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return maxId + 1;
    }

    //No method to write time offs exists yet, will be done on future use case
    public ArrayList<Timeoff> getTimeoffs() {
        ArrayList<Timeoff> timeoffs = new ArrayList<>();
        try (Scanner myReader = new Scanner(new File("timeoff.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Scanner tempScanner = new Scanner(data);
                int empId = tempScanner.nextInt();
                Timeoff timeoff = new Timeoff(empId); // startDate and endDate are not stored in the file
                while (tempScanner.hasNext()) {
                    String date = tempScanner.next();
                    timeoff.addDate(date);
                }
                timeoffs.add(timeoff);
                tempScanner.close();
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return timeoffs;
    } 

    public void writeTimeoff(Timeoff data) {
        try(FileWriter fwTimeoff = new FileWriter("timeoff.txt", true)){
            fwTimeoff.write(data.getData() + "\n");
            System.out.println("Successfully appended to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public ArrayList<BaseEmployee> getAllEmployeesExcluding(ArrayList<Integer> excludeList) {
        ArrayList<BaseEmployee> employees = new ArrayList<>();
        try (Scanner myReader = new Scanner(new File("employees.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                BaseEmployee emp = parseEmployee(data);
                if (!excludeList.contains(emp.getID())) {
                    employees.add(emp);
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return employees;
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

    public void writeSchedule(Schedule schedule){
        try(FileWriter fwSchedule = new FileWriter("schedules.txt", true)){
            fwSchedule.write(schedule.getData() + "\n");
            System.out.println("Successfully appended to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public int generateCustomerRewardsID() {
        int maxId = 0;
        try {
            ArrayList<Rewards> rewardsList = getCustomerRewards();
            for (Rewards r : rewardsList) {
                if (r.getId() > maxId) {
                    maxId = r.getId();
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return maxId + 1; // Return next available ID if not found
    }

    public void addCustomerToRewardsProgram(Customer customer) {
        try(FileWriter fwRewards = new FileWriter("rewards.txt", true)){
            Rewards rewards = customer.getRewards();
            fwRewards.write(rewards.getData() + "\n");
            System.out.println("Successfully appended to the rewards file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public Rewards getCustomerRewards(String phoneNumber) {
        Rewards rewards = null;
        ArrayList<Rewards> allRewards = getCustomerRewards();
        for (Rewards r : allRewards) {
            if (r.getPhoneNumber().equals(phoneNumber)) {
                rewards = r;
                break;
            }
        }
        return rewards;
    }

    public ArrayList<Rewards> getCustomerRewards(){
        ArrayList<Rewards> rewards = new ArrayList<>();
        try (Scanner myReader = new Scanner(new File("rewards.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Scanner tempScanner = new Scanner(data);
                int id = tempScanner.nextInt();
                int points = tempScanner.nextInt();
                String email = tempScanner.next();
                String phone = tempScanner.next();
                Rewards reward = new Rewards(id, points, email, phone);
                rewards.add(reward);
                tempScanner.close();
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return rewards;
    }

    public void updateCustomerRewardsPoints(Rewards rewards) {
        File inputFile = new File("rewards.txt");
        File tempFile = new File("temp_rewards.txt");

        try (
            Scanner myReader = new Scanner(inputFile);
            PrintWriter writer = new PrintWriter(tempFile);
        ) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Scanner tempScanner = new Scanner(data);
                int id = tempScanner.nextInt();
                int points = tempScanner.nextInt();
                String email = tempScanner.next();
                String phone = tempScanner.next();
                tempScanner.close();

                // Update points if ID matches
                if (id == rewards.getId()) {
                    points = rewards.getPoints();
                }

                // Write updated or original line to temp file
                writer.println(id + " " + points + " " + email + " " + phone);
            }
        } catch (Exception e) {
            System.out.println("An error occurred while updating rewards.");
            e.printStackTrace();
            return;
        }

        // Replace original file with the updated one
        if (!inputFile.delete()) {
            System.out.println("Could not delete original rewards file.");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp rewards file.");
        }
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
