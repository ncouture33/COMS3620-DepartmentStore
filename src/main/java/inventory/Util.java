package inventory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import inventory.io.InventoryFileStore;
import inventory.service.ReceivingService;
import inventory.service.PurchaseOrderService;
import inventory.ui.CreatePurchaseOrderUI;
import inventory.ui.EditPurchaseOrderUI;
import inventory.ui.CancelPurchaseOrderUI;

public class Util {

    public static void runInventory(Scanner passedScanner) {
        Scanner sc = passedScanner;

        // Shared file store and services for this module
        InventoryFileStore store = new InventoryFileStore(Paths.get("."));
        ReceivingService receivingService = new ReceivingService(store);
        PurchaseOrderService poService = new PurchaseOrderService(store);

        // UI handlers for the purchase order use case
        CreatePurchaseOrderUI createPoUI = new CreatePurchaseOrderUI(poService);
        EditPurchaseOrderUI editPoUI = new EditPurchaseOrderUI(poService);
        CancelPurchaseOrderUI cancelPoUI = new CancelPurchaseOrderUI(poService);

        while (true) {
            System.out.print("""
                    Inventory Menu
                    1: Process New Inventory (Use Case 5)
                    2: Create New Purchase Order
                    3: Manage Existing Purchase Order
                    back: Return to main menu
                    """);

            String cmd = sc.nextLine().trim();

            if ("back".equalsIgnoreCase(cmd)) {
                return;
            }

            try {
                switch (cmd) {
                    case "1" -> processNewInventory(sc, receivingService);
                    case "2" -> createPoUI.run(sc);
                    case "3" -> managePurchaseOrder(sc, editPoUI, cancelPoUI);
                    default  -> System.out.println("Unknown command, please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                // e.printStackTrace(); // uncomment if you want debug info
            }
        }
    }

    // ---------------- Use Case 5: Process New Inventory ----------------

    private static void processNewInventory(Scanner sc, ReceivingService service) {
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

        if (lines.isEmpty()) {
            System.out.println("No items entered. Nothing to process.");
            return;
        }

        var rec = service.processReceiving(poId, lines);

        System.out.println("Receiving saved for " + poId + ". Status = " + rec.getStatus());
        System.out.println("Check files in project folder:");
        System.out.println("  receivings-" + poId + ".txt");
        System.out.println("  damage_reports.txt (if any damaged)");
        System.out.println("  inventory.txt (onHand updated)");
    }

    // -------- Use Case X: Managing existing Purchase Orders (Alt Flow 1) --------

    private static void managePurchaseOrder(
            Scanner sc,
            EditPurchaseOrderUI editUI,
            CancelPurchaseOrderUI cancelUI
    ) {
        while (true) {
            System.out.print("""
                    
                    Manage Existing Purchase Order
                    1: Edit Purchase Order
                    2: Cancel Purchase Order
                    back: Return to Inventory Menu
                    """);

            String cmd = sc.nextLine().trim();

            if ("back".equalsIgnoreCase(cmd)) {
                return;
            }

            switch (cmd) {
                case "1" -> editUI.run(sc);
                case "2" -> cancelUI.run(sc);
                default  -> System.out.println("Unknown command, please try again.");
            }
        }
    }
}
