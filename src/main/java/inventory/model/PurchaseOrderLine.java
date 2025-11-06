package inventory.model;

import Utils.Data;

public class PurchaseOrderLine implements Data {
    private final String sku;
    private final int expectedQty;

    public PurchaseOrderLine(String sku, int expectedQty) {
        this.sku = sku;
        this.expectedQty = expectedQty;
    }

    public String getSku() { return sku; }
    public int getExpectedQty() { return expectedQty; }

    @Override
    public String getData() {
        return "ITEM:" + sku + "," + expectedQty;
    }

    public static PurchaseOrderLine parse(String line) {
        String[] split = line.substring("ITEM:".length()).split(",");
        if (split.length != 2) throw new IllegalArgumentException("Bad PO line: " + line);
        return new PurchaseOrderLine(split[0], Integer.parseInt(split[1]));
    }
}
