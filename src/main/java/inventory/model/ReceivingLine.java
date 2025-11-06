package inventory.model;

import Utils.Data;

public class ReceivingLine implements Data {
    private final String sku;
    private final int expectedQty;
    private final int receivedQty;
    private final int damagedQty;

    public ReceivingLine(String sku, int expectedQty, int receivedQty, int damagedQty) {
        this.sku = sku;
        this.expectedQty = expectedQty;
        this.receivedQty = receivedQty;
        this.damagedQty = damagedQty;
    }

    public String getSku() { return sku; }
    public int getExpectedQty() { return expectedQty; }
    public int getReceivedQty() { return receivedQty; }
    public int getDamagedQty() { return damagedQty; }
    public int getAcceptedQty() { return Math.max(0, receivedQty - damagedQty); }

    @Override
    public String getData() {
        return "RLINE:" + sku + "," + expectedQty + "," + receivedQty + "," + damagedQty;
    }

    public static ReceivingLine parse(String line) {
        String[] parts = line.substring("RLINE:".length()).split(",");
        if (parts.length != 4) throw new IllegalArgumentException("Bad receiving line: " + line);
        return new ReceivingLine(
                parts[0],
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        );
    }
}
