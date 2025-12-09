package Security;

import Security.ui.LogTheftUI;

import java.util.Scanner;

public class Util {

    public static void runSecurity(Scanner scanner) {
        while (true) {
            System.out.println("Security related actions:");
            System.out.println("1: Log a theft incident");
            System.out.println("0: Back");
            String command = scanner.nextLine();

            if (command.equals("1")) {
                TheftIncidentRepository repo = new FileTheftIncidentRepository("theft_incidents.txt");
                TheftLoggingService service = new TheftLoggingService(repo);
                LogTheftUI ui = new LogTheftUI(service, scanner);
                ui.run();
            } else if (command.equals("0")) {
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }
}
