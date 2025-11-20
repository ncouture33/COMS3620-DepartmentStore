package inventory.model;

import Utils.Data;

public class PurchaseOrderLine implements Data {
    private final String sku;
    private final int expectedQty;
    private final double unitCost;

    public PurchaseOrderLine(String sku, int expectedQty, double unitCost) {
        this.sku = sku;
        this.expectedQty = expectedQty;
        this.unitCost = unitCost;
    }

    public PurchaseOrderLine(String sku, int expectedQty) {
        this(sku, expectedQty, 0.0);
    }

    public String getSku() { return sku; }
    public int getExpectedQty() { return expectedQty; }
    public double getUnitCost() { return unitCost; }

    @Override
    public String getData() {
        return "ITEM:" + sku + "," + expectedQty + "," + unitCost;
    }

    public static PurchaseOrderLine parse(String line) {
        String[] parts = line.substring("ITEM:".length()).split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Bad PO line: " + line);
        }
        String sku = parts[0];
        int qty = Integer.parseInt(parts[1]);
        double unitCost = Double.parseDouble(parts[2]);
        return new PurchaseOrderLine(sku, qty, unitCost);
    }
}
