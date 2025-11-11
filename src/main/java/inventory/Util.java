package inventory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import inventory.io.InventoryFileStore;
import inventory.service.ReceivingService;

public class Util {
    public static void runInventory(Scanner passedScanner) {
        Scanner sc = passedScanner;
        while (true) {
            System.out.print("""
                    Inventory Menu
                    1: Process New Inventory (Use Case 5)
                    back: Return to main menu
                    """);
            String cmd = sc.nextLine().trim();

            if ("back".equalsIgnoreCase(cmd)) return;

            if ("1".equals(cmd)) {
                processNewInventory(sc);
                continue;
            }
        }
    }

    private static void processNewInventory(Scanner sc) {
        System.out.print("Enter Purchase Order ID: ");
        String poId = sc.nextLine().trim();

        List<ReceivingService.CountLine> lines = new ArrayList<>();
        System.out.println("Enter lines as: <SKU> <receivedQty> <damagedQty>. Blank line to finish.");
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) break;
            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Format: SKU RECEIVED DAMAGED");
                continue;
            }
            String sku = parts[0];
            int received = Integer.parseInt(parts[1]);
            int damaged = Integer.parseInt(parts[2]);
            lines.add(new ReceivingService.CountLine(sku, received, damaged));
        }

        InventoryFileStore store = new InventoryFileStore(Paths.get("."));
        ReceivingService service = new ReceivingService(store);
        var rec = service.processReceiving(poId, lines);

        System.out.println("Receiving saved for " + poId + ". Status = " + rec.getStatus());
        System.out.println("Check files in project folder:");
        System.out.println("  receivings-" + poId + ".txt");
        System.out.println("  damage_reports.txt (if any damaged)");
        System.out.println("  inventory.txt (onHand updated)");
    }
}
