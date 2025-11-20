package inventory.ui;

import inventory.model.PurchaseOrder;
import inventory.model.PurchaseOrderLine;
import inventory.model.PurchaseOrderStatus;
import inventory.service.PurchaseOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class EditPurchaseOrderUI {

    private final PurchaseOrderService svc;

    public EditPurchaseOrderUI(PurchaseOrderService svc) {
        this.svc = svc;
    }

    public void run(Scanner scanner) {
        System.out.println("\n--- Edit Existing Purchase Order ---");
        System.out.print("Enter PO id to edit: ");
        String poId = scanner.nextLine().trim();

        Optional<PurchaseOrder> poOpt = svc.loadPurchaseOrder(poId);
        if (poOpt.isEmpty()) {
            System.out.println("Purchase order not found: " + poId);
            return;
        }

        PurchaseOrder po = poOpt.get();
        if (po.getStatus() == PurchaseOrderStatus.CANCELED) {
            System.out.println("PO is canceled and cannot be edited.");
            return;
        }

        System.out.println("Current lines:");
        for (PurchaseOrderLine line : po.getLines()) {
            System.out.printf("  SKU %s -> qty=%d, unit=%.2f%n",
                    line.getSku(), line.getExpectedQty(), line.getUnitCost());
        }

        System.out.println("Enter new values. Blank = keep existing. 0 qty = remove line.");

        List<PurchaseOrderLine> newLines = new ArrayList<>();

        // Edit existing lines
        for (PurchaseOrderLine oldLine : po.getLines()) {
            String sku = oldLine.getSku();
            int currentQty = oldLine.getExpectedQty();
            double currentCost = oldLine.getUnitCost();

            System.out.printf("New quantity for %s (current %d): ", sku, currentQty);
            String qtyInput = scanner.nextLine().trim();

            int newQty;
            if (qtyInput.isEmpty()) {
                newQty = currentQty;
            } else {
                newQty = Integer.parseInt(qtyInput);
            }

            if (newQty <= 0) {
                continue; // remove this line
            }

            System.out.printf("New unit cost for %s (current %.2f): ", sku, currentCost);
            String costInput = scanner.nextLine().trim();

            double newCost;
            if (costInput.isEmpty()) {
                newCost = currentCost;
            } else {
                newCost = Double.parseDouble(costInput);
            }

            newLines.add(new PurchaseOrderLine(sku, newQty, newCost));
        }

        System.out.print("Add new line items? (y/n): ");
        String addMore = scanner.nextLine().trim();
        if (addMore.equalsIgnoreCase("y")) {
            while (true) {
                System.out.print("Enter new SKU (or blank to stop adding): ");
                String newSku = scanner.nextLine().trim();
                if (newSku.isEmpty())
                    break;

                System.out.print("Enter quantity for " + newSku + ": ");
                int qty = Integer.parseInt(scanner.nextLine().trim());

                System.out.print("Enter unit cost for " + newSku + ": ");
                double cost = Double.parseDouble(scanner.nextLine().trim());

                if (qty > 0) {
                    newLines.add(new PurchaseOrderLine(newSku, qty, cost));
                }
            }
        }

        if (newLines.isEmpty()) {
            System.out.println("All lines removed. Cannot save an empty PO; no changes applied.");
            return;
        }

        PurchaseOrder updated = svc.updatePurchaseOrderLines(poId, newLines);
        var summary = svc.summarizeSaved(updated);

        System.out.println("\nUpdated PO " + summary.id + " (status " + summary.status + "):");
        for (var l : summary.lines) {
            System.out.printf("  %s | %s | qty=%d | unit=%.2f | line=%.2f%n",
                    l.sku, l.description, l.quantity, l.unitCost, l.lineTotal);
        }
        System.out.printf("TOTAL: %.2f%n", summary.totalCost);
    }
}
