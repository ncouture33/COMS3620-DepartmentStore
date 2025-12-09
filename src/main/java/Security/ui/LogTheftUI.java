package Security.ui;

import Security.TheftLoggingService;

import java.util.Scanner;

public class LogTheftUI {

    private final TheftLoggingService service;
    private final Scanner scanner;

    public LogTheftUI(TheftLoggingService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("--- Log Theft Incident ---");

        System.out.print("Enter date and time of theft: ");
        String dateTime = scanner.nextLine();

        System.out.print("Enter store location: ");
        String location = scanner.nextLine();

        System.out.println("Describe stolen items: ");
        String stolenItems = scanner.nextLine();

        boolean hasCameraFootage = askYesNo("Was camera footage available? (y/n): ");
        boolean hasWitnesses = askYesNo("Were there witnesses? (y/n): ");

        System.out.print("Enter police report number: ");
        String policeReportNumber = scanner.nextLine();

        service.logTheft(
                dateTime,
                location,
                stolenItems,
                hasCameraFootage,
                hasWitnesses,
                policeReportNumber);

        System.out.println("Theft incident logged.");
    }

    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes"))
                return true;
            if (input.equals("n") || input.equals("no"))
                return false;
            System.out.println("Please enter y or n.");
        }
    }
}
