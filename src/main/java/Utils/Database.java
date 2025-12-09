package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
import StoreOperations.ClockTime;

public class Database implements DatabaseWriter {

    @Override
    public void writeEmployee(BaseEmployee data) {
        try (FileWriter fwEmployee = new FileWriter("employees.txt", true)) {
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
    public void writeRegisterCredentials(BaseEmployee data) {
        File credFile = new File("registerEmployees.txt");
        java.util.ArrayList<String> keep = new java.util.ArrayList<>();
        String newUsername = data.getUsername() == null ? "" : data.getUsername();
        int newId = data.getID();

        // read existing lines and keep those that do not belong to this employee id or
        // username
        if (credFile.exists()) {
            try (Scanner reader = new Scanner(credFile)) {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine().trim();
                    if (line.isEmpty())
                        continue;
                    try (Scanner s = new Scanner(line)) {
                        int id = s.nextInt();
                        String username = s.hasNext() ? s.next() : "";
                        // skip entries matching this id or username
                        if (id == newId)
                            continue;
                        if (!username.isEmpty() && username.equals(newUsername))
                            continue;
                        keep.add(line);
                    } catch (Exception ex) {

                        // if malformed, keep the line to avoid data loss
                        keep.add(line);
                    }
                }
            } catch (Exception e) {
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
        try (FileWriter fw = new FileWriter(credFile, false)) {
            for (String l : keep)
                fw.write(l + "\n");
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
                if (emp.getID() == empId) {
                    try (FileWriter fwEmployee = new FileWriter("employeesInOffboardingProcess.txt", true)) {
                        OffboardingEmployee offemp = new OffboardingEmployee(empId, emp.getFName(), emp.getLName(),
                                emp.getDOB(), emp.getSocial(), emp.getDepartment(),emp.getRole(), date, reasonForLeaving);
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

        // Attempt to load stored credentials from registerEmployees.txt and attach them
        // to parsed employees
        java.util.ArrayList<String[]> creds = new java.util.ArrayList<>();
        try (Scanner credReader = new Scanner(new File("registerEmployees.txt"))) {
            while (credReader.hasNextLine()) {
                String line = credReader.nextLine().trim();

                if (line.isEmpty())
                    continue;

                try (Scanner s = new Scanner(line)) {
                    int id = s.nextInt();
                    String username = s.hasNext() ? s.next() : "";
                    String salt = s.hasNext() ? s.next() : "";
                    String hash = s.hasNext() ? s.next() : "";
                    String pin = s.hasNext() ? s.next() : "";
                    creds.add(new String[] { String.valueOf(id), username, salt, hash, pin });
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
                if (c.length > 1 && c[1] != null && !c[1].isEmpty())
                    emp.setUsername(c[1]);
                if (c.length > 3 && c[2] != null && !c[2].isEmpty() && c[3] != null && !c[3].isEmpty())
                    emp.setStoredPassword(c[2], c[3]);
                if (c.length > 4 && c[4] != null && !c[4].isEmpty())
                    emp.setPin(c[4]);
            }
        }

        return employees;
    }

    @Override
    public ArrayList<ClockTime> getClockedEmployees() {
        ArrayList<ClockTime> clockedEmployees = new ArrayList<>();

        try (Scanner myReader = new Scanner(new File("clockedInEmployees.txt"))) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                ClockTime ct = parseClockTime(data);
                if (ct != null) {
                    clockedEmployees.add(ct);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading clocked-in employees");
            e.printStackTrace();
        }
        return clockedEmployees;
    }

    private ClockTime parseClockTime(String data) {
        String[] parts = data.trim().split("\\s+");
        if (parts.length < 3) {
            System.out.println("Invalid line: " + data);
            return null;
        }

        int social = Integer.parseInt(parts[0]);
        int date = Integer.parseInt(parts[1]);
        String clockInTime = parts[2];

        return new ClockTime(social, date, clockInTime);
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
    Scanner scanner = new Scanner(data);
    if (!scanner.hasNext()) return null;

    String empType = scanner.next();

    int id = scanner.hasNextInt() ? scanner.nextInt() : 0;
    String fName = scanner.hasNext() ? scanner.next() : "";
    String lName = scanner.hasNext() ? scanner.next() : "";
    int DOB = scanner.hasNextInt() ? scanner.nextInt() : 0;
    int social = scanner.hasNextInt() ? scanner.nextInt() : 0;
    int timePeriod = scanner.hasNextInt() ? scanner.nextInt() : 0;
    double hoursWorked = scanner.hasNextDouble() ? scanner.nextDouble() : 0.0;
    double overtimeHours = scanner.hasNextDouble() ? scanner.nextDouble() : 0.0;

    String bankName = scanner.hasNext() ? scanner.next() : "";
    int routingNum = scanner.hasNextInt() ? scanner.nextInt() : 0;
    int accountNum = scanner.hasNextInt() ? scanner.nextInt() : 0;
    Account account = new Account(bankName, routingNum, accountNum);

    double hourlyRate = 0.0, overtimeRate = 0.0;
    int salaryAmount = 0;

    if (empType.equals("SALARY") && scanner.hasNextInt()) {
        salaryAmount = scanner.nextInt();
    } else if (empType.equals("HOURLY")) {
        if (scanner.hasNextDouble()) hourlyRate = scanner.nextDouble();
        if (scanner.hasNextDouble()) overtimeRate = scanner.nextDouble();
    }

    // Everything else is department and role (possibly multi-word)
    String dep = "";
    String role = "";
    if (scanner.hasNextLine()) {
        String remaining = scanner.nextLine().trim();
        if (!remaining.isEmpty()) {
            String[] parts = remaining.split("\\s+");
            if (parts.length >= 2) {
                role = parts[parts.length - 1]; // last word
                dep = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
            } else if (parts.length == 1) {
                dep = parts[0]; // or role depending on your convention
            }
        }
    }

    BaseEmployee emp = null;
    if (empType.equals("SALARY")) {
        emp = new Salary(id, fName, lName, DOB, social, salaryAmount, dep, role);
    } else if (empType.equals("HOURLY")) {
        emp = new Hourly(id, fName, lName, DOB, social, hourlyRate, overtimeRate, dep, role);
    }

    if (emp != null) {
        emp.setAccount(account);
        emp.setTimeCard(new TimeCard(timePeriod, hoursWorked, overtimeHours));
    }

    scanner.close();
    return emp;
}


    private OffboardingEmployee parseOffBoardEmployee(String data) {
        // Parsing logic here
        Scanner tempScanner = new Scanner(data);
        int id = tempScanner.nextInt();
        String fName = tempScanner.next();
        String lName = tempScanner.next();
        int DOB = tempScanner.nextInt();
        int social = tempScanner.nextInt();
        String dep = tempScanner.nextLine();
        String role = tempScanner.nextLine();
        String date = tempScanner.next();
        String reasonForLeaving = "";
        if (tempScanner.hasNextLine()) {
            reasonForLeaving = tempScanner.nextLine().trim();
        }

        OffboardingEmployee emp = new OffboardingEmployee(id, fName, lName, DOB, social, dep, role, date, reasonForLeaving);
        tempScanner.close();
        return emp;
    }

    public ArrayList<Schedule> getSchedules() {
        ArrayList<Schedule> schedules = new ArrayList<>();
        try (Scanner myReader = new Scanner(new File("schedules.txt"))) {
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine().trim();
                if (line.isEmpty())
                    continue; // skip blank separators

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
                    if (nextLine.isEmpty())
                        continue; // ignore stray blanks
                    if (nextLine.equals("ENDSHIFTS"))
                        break;

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

    public int getNextScheduleID() {
        int maxId = 0;
        try {
            ArrayList<Schedule> scheds = getSchedules();
            for (Schedule sched : scheds) {
                if (sched.getScheduleId() > maxId) {
                    maxId = sched.getScheduleId();
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return maxId + 1;
    }

    // No method to write time offs exists yet, will be done on future use case
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
        try (FileWriter fwTimeoff = new FileWriter("timeoff.txt", true)) {
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

    public void writeSchedule(Schedule schedule) {
        try (FileWriter fwSchedule = new FileWriter("schedules.txt", true)) {
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
        try (FileWriter fwRewards = new FileWriter("rewards.txt", true)) {
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

    public ArrayList<Rewards> getCustomerRewards() {
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
                PrintWriter writer = new PrintWriter(tempFile);) {
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
    public boolean clockOutEmployee(ClockTime emp) {
        File inputFile = new File("clockedInEmployees.txt");
        File tempFile = new File("clockedInEmployees_temp.txt");

        String socialString = String.valueOf(emp.getSocial());
        boolean removed = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().isEmpty()) {
                    // skip empty lines
                    continue;
                }

                // split on whitespace
                String[] parts = currentLine.trim().split("\\s+");
                String firstToken = parts.length > 0 ? parts[0] : "";

                if (socialString.equals(firstToken)) {
                    // removes the line
                    removed = true;
                    continue;
                }

                writer.println(currentLine);
            }

            writer.flush();
        } catch (IOException e) {
            System.out.println("An error occurred while processing clocked-in file.");
            e.printStackTrace();
            return false;
        }

        // replace the original file with temp file
        try {
            Path source = tempFile.toPath();
            Path target = inputFile.toPath();
            // Use REPLACE_EXISTING to overwrite
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            // fallback if atomic move isn't supported on the filesystem
            try {
                Files.move(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Could not replace original file with temp file.");
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            System.out.println("Could not replace original file with temp file.");
            e.printStackTrace();
            return false;
        }

        if (removed) {
            System.out.println("Employee clocked out");
        } else {
            System.out.println("Employee was not clocked in.");
        }

        return removed;
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

    @Override
public void updateEmployee(BaseEmployee updatedEmp) {
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
                emp = parseEmployee(data);
            } catch (Exception e) {
                System.out.println("Skipping malformed line: " + data);
                writer.println(data); // keep original line
                continue;
            }

            if (emp.getID() == updatedEmp.getID()) {
                // write updated employee instead
                writer.println(updatedEmp.getData());
            } else {
                // keep other employees as is
                writer.println(data);
            }
        }
    } catch (Exception e) {
        System.out.println("An error occurred while updating employee.");
        e.printStackTrace();
        return;
    }

    // Replace original file with temp
    if (!inputFile.delete()) {
        System.out.println("Could not delete original file.");
        return;
    }
    if (!tempFile.renameTo(inputFile)) {
        System.out.println("Could not rename temp file.");
    }

    // Also update credentials if needed
    writeRegisterCredentials(updatedEmp);
}


    @Override
    public BaseEmployee getEmployeeByID(int empID) {
    try (Scanner myReader = new Scanner(new File("employees.txt"))) {
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine().trim();
            if (data.isEmpty()) continue;

            BaseEmployee emp;
            try {
                emp = parseEmployee(data);
            } catch (Exception e) {
                System.out.println("Skipping malformed line: " + data);
                e.printStackTrace();
                continue;
            }

            if (emp.getID() == empID) {
                // Load credentials for this employee if present
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

                            if (id == empID) {
                                if (!username.isEmpty()) emp.setUsername(username);
                                if (!salt.isEmpty() && !hash.isEmpty()) emp.setStoredPassword(salt, hash);
                                if (!pin.isEmpty()) emp.setPin(pin);
                                break;
                            }
                        } catch (Exception ex) {
                            // skip malformed lines
                        }
                    }
                } catch (Exception e) {
                    // ignore if file doesn't exist
                }
                return emp;
            }
        }
    } catch (Exception e) {
        System.out.println("Error reading employees.txt");
        e.printStackTrace();
    }
    return null; // not found
}


}
