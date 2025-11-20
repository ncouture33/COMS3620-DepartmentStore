package inventory.ui;

import inventory.ProductDesign.ProductApprovalService;

import java.util.Scanner;

public class ProductApprovalUI {

    private final ProductApprovalService service;

    public ProductApprovalUI(ProductApprovalService service) {
        this.service = service;
    }

    public void run(Scanner sc) {
        while (true) {
            System.out.print("""

                    Approve / Reject New Product (Use Case 6)
                    1: Submit Product Design for Approval
                    2: Approve Product Design
                    3: Reject Product Design
                    back: Return to Inventory Menu
                    """);

            String cmd = sc.nextLine().trim();

            if (cmd.equalsIgnoreCase("back")) {
                return;
            }

            try {
                switch (cmd) {
                    case "1" -> submit(sc);
                    case "2" -> approve(sc);
                    case "3" -> reject(sc);
                    default -> System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void submit(Scanner sc) {
        System.out.print("Enter Product Design ID: ");
        String id = sc.nextLine().trim();
        service.submitForApproval(id);
        System.out.println("Submitted " + id + " for approval.");
    }

    private void approve(Scanner sc) {
        System.out.print("Enter Product Design ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Enter Manager ID: ");
        String manager = sc.nextLine().trim();
        service.approveProduct(id, manager);
        System.out.println(id + " approved.");
    }

    private void reject(Scanner sc) {
        System.out.print("Enter Product Design ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Enter Manager ID: ");
        String manager = sc.nextLine().trim();
        System.out.print("Enter rejection comment: ");
        String comment = sc.nextLine().trim();
        service.rejectProduct(id, manager, comment);
        System.out.println(id + " rejected.");
    }
}
