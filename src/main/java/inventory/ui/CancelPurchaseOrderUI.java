package inventory.ui;

import inventory.model.PurchaseOrder;
import inventory.service.PurchaseOrderService;

import java.util.Scanner;

public class CancelPurchaseOrderUI {

    private final PurchaseOrderService svc;

    public CancelPurchaseOrderUI(PurchaseOrderService svc) {
        this.svc = svc;
    }

    public void run(Scanner scanner) {
        System.out.println("\n--- Cancel Purchase Order ---");
        System.out.print("Enter PO id to cancel: ");
        String poId = scanner.nextLine().trim();

        PurchaseOrder canceled = svc.cancelPurchaseOrder(poId);
        System.out.println("PO " + canceled.getId() + " status is now " + canceled.getStatus()
                + ". Canceled POs are not used in Processing New Inventory.");
    }
}
