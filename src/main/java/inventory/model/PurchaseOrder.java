package inventory.model;

import Utils.Data;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder implements Data {

    private final String id;
    private PurchaseOrderStatus status;
    private final List<PurchaseOrderLine> lines = new ArrayList<>();

    /**
     * Convenience constructor: new PO with given id, default status OPEN.
     */
    public PurchaseOrder(String id) {
        this(id, PurchaseOrderStatus.OPEN);
    }

    /**
     * Full constructor used when loading/saving from file.
     */
    public PurchaseOrder(String id, PurchaseOrderStatus status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }

    public List<PurchaseOrderLine> getLines() {
        return lines;
    }

    public void addLine(PurchaseOrderLine line) {
        lines.add(line);
    }

    @Override
    public String getData() {
        /*
         * purchase_orders.txt format (one block per PO):
         *
         * PO:<id>|<status>
         * ITEM:<sku>,<expectedQty>,<unitCost>
         * ...
         * END
         */
        StringBuilder sb = new StringBuilder();
        sb.append("PO:")
                .append(id)
                .append("|")
                .append(status == null ? PurchaseOrderStatus.OPEN : status)
                .append("\n");

        for (PurchaseOrderLine l : lines) {
            sb.append(l.getData()).append("\n");
        }

        sb.append("END");
        return sb.toString();
    }

    public static PurchaseOrder parseBlock(List<String> block) {
        if (block == null || block.isEmpty() || !block.get(0).startsWith("PO:")) {
            throw new IllegalArgumentException("Bad PO block");
        }

        // Expect: "PO:<id>|<status>"
        String header = block.get(0).substring("PO:".length());
        String[] parts = header.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Bad PO header (expected PO:<id>|<status>): " + block.get(0));
        }

        String id = parts[0];
        PurchaseOrderStatus status = PurchaseOrderStatus.valueOf(parts[1]);

        PurchaseOrder po = new PurchaseOrder(id, status);

        for (int i = 1; i < block.size(); i++) {
            String line = block.get(i);
            if ("END".equals(line))
                break;
            if (line.startsWith("ITEM:")) {
                po.addLine(PurchaseOrderLine.parse(line));
            }
        }

        return po;
    }
}
