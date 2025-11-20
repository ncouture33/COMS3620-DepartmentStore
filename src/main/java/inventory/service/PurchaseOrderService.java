package inventory.service;

import inventory.io.InventoryFileStore;
import inventory.model.*;

import java.util.*;

public class PurchaseOrderService {

    private final InventoryFileStore store;

    public PurchaseOrderService(InventoryFileStore store) {
        this.store = store;
    }

    // ---------------- Draft PO (no vendor) ----------------

    public static class DraftPurchaseOrder {
        private final List<PurchaseOrderLine> lines = new ArrayList<>();

        public List<PurchaseOrderLine> getLines() {
            return lines;
        }

        public void addLine(PurchaseOrderLine line) {
            lines.add(line);
        }
    }

    // ---------------- DTOs for UI ----------------

    public static class LineSummary {
        public final String sku;
        public final String description;
        public final int quantity;
        public final double unitCost;
        public final double lineTotal;

        public LineSummary(String sku, String description, int quantity, double unitCost) {
            this.sku = sku;
            this.description = description;
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.lineTotal = unitCost * quantity;
        }
    }

    public static class PurchaseOrderSummary {
        public final String id; // null for draft
        public final PurchaseOrderStatus status; // null for draft
        public final List<LineSummary> lines;
        public final double totalCost;

        public PurchaseOrderSummary(String id,
                PurchaseOrderStatus status,
                List<LineSummary> lines) {
            this.id = id;
            this.status = status;
            this.lines = lines;
            this.totalCost = lines.stream().mapToDouble(l -> l.lineTotal).sum();
        }
    }

    // ---------------- Main flow ----------------

    public DraftPurchaseOrder startNewPurchaseOrder() {
        return new DraftPurchaseOrder();
    }

    public LineSummary addLineToDraft(DraftPurchaseOrder draft, String sku, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");

        // Use product catalog (products.txt), not inventory.txt
        Map<String, Product> catalog = store.loadProductCatalog();
        Product product = catalog.get(sku);
        if (product == null) {
            throw new IllegalArgumentException("Unknown SKU: " + sku + ". Product must be known in the system.");
        }

        double unitCost = product.getUnitPrice();
        PurchaseOrderLine line = new PurchaseOrderLine(sku, quantity, unitCost);
        draft.addLine(line);

        return new LineSummary(
                sku,
                product.getName(),
                quantity,
                unitCost);
    }

    public PurchaseOrderSummary summarizeDraft(DraftPurchaseOrder draft) {
        Map<String, Product> catalog = store.loadProductCatalog();
        List<LineSummary> summaries = new ArrayList<>();

        for (PurchaseOrderLine line : draft.getLines()) {
            Product p = catalog.get(line.getSku());
            String name = (p != null) ? p.getName() : "UNKNOWN";
            double unitCost = line.getUnitCost(); // use stored PO cost
            summaries.add(new LineSummary(line.getSku(), name, line.getExpectedQty(), unitCost));
        }

        return new PurchaseOrderSummary(
                null,
                null,
                summaries);
    }

    public PurchaseOrder saveDraft(DraftPurchaseOrder draft) {
        if (draft.getLines().isEmpty()) {
            throw new IllegalStateException("Cannot save an empty purchase order.");
        }

        String poId = generateNewPurchaseOrderId();

        PurchaseOrder po = new PurchaseOrder(poId, PurchaseOrderStatus.OPEN);
        for (PurchaseOrderLine l : draft.getLines()) {
            po.addLine(l);
        }

        store.addPurchaseOrder(po);
        return po;
    }

    private String generateNewPurchaseOrderId() {
        List<PurchaseOrder> all = store.loadAllPurchaseOrders();
        int max = 0;
        for (PurchaseOrder po : all) {
            String id = po.getId();
            try {
                String[] parts = id.split("-");
                int num = Integer.parseInt(parts[parts.length - 1]);
                if (num > max)
                    max = num;
            } catch (Exception ignored) {
            }
        }
        int next = max + 1;
        return String.format("PO-%04d", next);
    }

    // ---------------- Edit / cancel ----------------

    public Optional<PurchaseOrder> loadPurchaseOrder(String poId) {
        return store.findPurchaseOrder(poId);
    }

    public PurchaseOrder updatePurchaseOrderLines(String poId, List<PurchaseOrderLine> newLines) {
        if (newLines == null || newLines.isEmpty()) {
            throw new IllegalStateException("Updated PO would have no line items.");
        }

        PurchaseOrder po = store.findPurchaseOrder(poId)
                .orElseThrow(() -> new IllegalArgumentException("PO not found: " + poId));

        if (po.getStatus() == PurchaseOrderStatus.CANCELED) {
            throw new IllegalStateException("Cannot edit a canceled purchase order.");
        }

        po.getLines().clear();
        po.getLines().addAll(newLines);

        store.upsertPurchaseOrder(po);
        return po;
    }

    public PurchaseOrder cancelPurchaseOrder(String poId) {
        PurchaseOrder po = store.findPurchaseOrder(poId)
                .orElseThrow(() -> new IllegalArgumentException("PO not found: " + poId));

        po.setStatus(PurchaseOrderStatus.CANCELED);
        store.upsertPurchaseOrder(po);

        return po;
    }

    public PurchaseOrderSummary summarizeSaved(PurchaseOrder po) {
        Map<String, Product> catalog = store.loadProductCatalog();
        List<LineSummary> lines = new ArrayList<>();
        for (PurchaseOrderLine l : po.getLines()) {
            Product p = catalog.get(l.getSku());
            String name = (p != null) ? p.getName() : "UNKNOWN";
            double unitCost = l.getUnitCost();
            lines.add(new LineSummary(l.getSku(), name, l.getExpectedQty(), unitCost));
        }
        return new PurchaseOrderSummary(po.getId(), po.getStatus(), lines);
    }
}
