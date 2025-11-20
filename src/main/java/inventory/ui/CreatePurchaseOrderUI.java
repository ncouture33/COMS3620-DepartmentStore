package inventory.ui;

import inventory.model.PurchaseOrder;
import inventory.service.PurchaseOrderService;

import java.util.Scanner;

public class CreatePurchaseOrderUI {

    private final PurchaseOrderService svc;

    public CreatePurchaseOrderUI(PurchaseOrderService svc) {
        this.svc = svc;
    }

    public void run(Scanner scanner) {
        System.out.println("\n--- Create New Purchase Order ---");

        // System opens unsaved PO form (no vendor)
        var draft = svc.startNewPurchaseOrder();

        // Enter SKU + quantity, add to PO
        while (true) {
            System.out.print("Enter SKU (or blank to finish): ");
            String sku = scanner.nextLine().trim();
            if (sku.isEmpty())
                break;

            System.out.print("Enter quantity for " + sku + ": ");
            int qty = Integer.parseInt(scanner.nextLine().trim());

            var lineSummary = svc.addLineToDraft(draft, sku, qty);

            System.out.printf(
                    "Added line: %s | %s | qty=%d | unit=%.2f | line total=%.2f%n",
                    lineSummary.sku,
                    lineSummary.description,
                    lineSummary.quantity,
                    lineSummary.unitCost,
                    lineSummary.lineTotal);
        }

        // Cancel before saving
        if (draft.getLines().isEmpty()) {
            System.out.println("No lines added. Draft discarded (no PO created).");
            return;
        }

        // Summary and review
        var summary = svc.summarizeDraft(draft);
        System.out.println("\n--- Draft Purchase Order Review ---");
        for (var l : summary.lines) {
            System.out.printf("  %s | %s | qty=%d | unit=%.2f | line=%.2f%n",
                    l.sku, l.description, l.quantity, l.unitCost, l.lineTotal);
        }
        System.out.printf("TOTAL: %.2f%n", summary.totalCost);

        // Save or discard
        System.out.print("Save this purchase order? (y/n): ");
        String answer = scanner.nextLine().trim();
        if (!answer.equalsIgnoreCase("y")) {
            System.out.println("Draft discarded (no PO created).");
            return;
        }

        PurchaseOrder saved = svc.saveDraft(draft);
        System.out.println("Purchase order saved with id: " + saved.getId()
                + " (status = " + saved.getStatus() + ")");
        System.out.println("It is now available to the warehouse for Processing New Inventory.");
    }
}
