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
import inventory.ui.ProductApprovalUI;

import inventory.ProductDesign.ProductApprovalService;
import inventory.ProductDesign.ProductRepository;
import inventory.ProductDesign.FileProductRepository;
import inventory.ProductDesign.ProductDesign;

public class Util {

    public static void runInventory(Scanner passedScanner) {
        Scanner sc = passedScanner;

        InventoryFileStore store = new InventoryFileStore(Paths.get("."));
        ReceivingService receivingService = new ReceivingService(store);
        PurchaseOrderService poService = new PurchaseOrderService(store);

        CreatePurchaseOrderUI createPoUI = new CreatePurchaseOrderUI(poService);
        EditPurchaseOrderUI editPoUI = new EditPurchaseOrderUI(poService);
        CancelPurchaseOrderUI cancelPoUI = new CancelPurchaseOrderUI(poService);

        ProductRepository productRepository = new FileProductRepository("product_designs.txt");

        ProductApprovalService.NotificationService notificationService = new ConsoleNotificationService();

        ProductApprovalService.ManufacturingGateway manufacturingGateway = new ConsoleManufacturingGateway();

        ProductApprovalService.DocumentationLockService documentationLockService = new ConsoleDocumentationLockService();

        ProductApprovalService productApprovalService = new ProductApprovalService(
                productRepository,
                notificationService,
                manufacturingGateway,
                documentationLockService);

        ProductApprovalUI productApprovalUI = new ProductApprovalUI(productApprovalService);

        while (true) {
            System.out.print("""
                    Inventory Menu
                    1: Process New Inventory (Use Case 5)
                    2: Create New Purchase Order
                    3: Manage Existing Purchase Order (Use Case 7)
                    4: Approve / Reject New Product (Use Case 6)
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
                    case "4" -> productApprovalUI.run(sc);
                    default -> System.out.println("Unknown command, please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void processNewInventory(Scanner sc, ReceivingService service) {
        System.out.print("Enter Purchase Order ID: ");
        String poId = sc.nextLine().trim();

        List<ReceivingService.CountLine> lines = new ArrayList<>();
        System.out.println("Enter lines as: <SKU> <receivedQty> <damagedQty>. Blank line to finish.");
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty())
                break;

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
        System.out.println("  damage_reports.txt");
        System.out.println("  inventory.txt");
    }

    private static void managePurchaseOrder(
            Scanner sc,
            EditPurchaseOrderUI editUI,
            CancelPurchaseOrderUI cancelUI) {
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
                default -> System.out.println("Unknown command, please try again.");
            }
        }
    }

    static class ConsoleNotificationService implements ProductApprovalService.NotificationService {
        @Override
        public void notifyManagerNewProductReady(ProductDesign design) {
            System.out.println("[NOTIFY MANAGER] Product ready: " + design.getId() + " - " + design.getName());
        }

        @Override
        public void notifyDesignerApproved(ProductDesign design, String managerId) {
            System.out.println("[NOTIFY DESIGNER] Product " + design.getId() + " approved by " + managerId);
        }

        @Override
        public void notifyDesignerRejected(ProductDesign design, String managerId) {
            System.out.println("[NOTIFY DESIGNER] Product " + design.getId() + " rejected by " + managerId);
            System.out.println("Reason: " + design.getManagerComment());
        }
    }

    static class ConsoleManufacturingGateway implements ProductApprovalService.ManufacturingGateway {
        @Override
        public void sendToManufacturer(ProductDesign design) {
            System.out.println("[MANUFACTURING] Sending design " + design.getId() + " to manufacturer...");
        }
    }

    static class ConsoleDocumentationLockService implements ProductApprovalService.DocumentationLockService {
        @Override
        public void lock(String documentationPath) {
            System.out.println("[DOCS] Locked: " + documentationPath);
        }

        @Override
        public void unlock(String documentationPath) {
            System.out.println("[DOCS] Unlocked: " + documentationPath);
        }
    }
}
