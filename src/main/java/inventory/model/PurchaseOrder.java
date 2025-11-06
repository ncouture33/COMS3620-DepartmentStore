package inventory.model;

import Utils.Data;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder implements Data {
    private final String id;
    private final List<PurchaseOrderLine> lines = new ArrayList<>();

    public PurchaseOrder(String id) { this.id = id; }

    public String getId() { return id; }
    public List<PurchaseOrderLine> getLines() { return lines; }
    public void addLine(PurchaseOrderLine line) { lines.add(line); }

    @Override
    public String getData() {
        StringBuilder sb = new StringBuilder();
        sb.append("PO:").append(id).append("\n");
        for (PurchaseOrderLine l : lines) sb.append(l.getData()).append("\n");
        sb.append("END");
        return sb.toString();
    }

    public static PurchaseOrder parseBlock(List<String> block) {
        if (block.isEmpty() || !block.get(0).startsWith("PO:")) {
            throw new IllegalArgumentException("Bad PO block");
        }
        String id = block.get(0).substring(3);
        PurchaseOrder po = new PurchaseOrder(id);
        for (int i = 1; i < block.size(); i++) {
            String line = block.get(i);
            if ("END".equals(line)) break;
            if (line.startsWith("ITEM:")) po.addLine(PurchaseOrderLine.parse(line));
        }
        return po;
    }
}
