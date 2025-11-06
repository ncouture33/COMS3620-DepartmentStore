package inventory.model;

import Utils.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReceivingRecord implements Data {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String purchaseOrderId;
    private String receivedAt;
    private final List<ReceivingLine> lines = new ArrayList<>();
    private ReceivingStatus status;

    public ReceivingRecord(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
        this.receivedAt = LocalDateTime.now().format(FMT);
        this.status = ReceivingStatus.CONFIRMED;
    }

    public String getPurchaseOrderId() { return purchaseOrderId; }
    public String getReceivedAt() { return receivedAt; }
    public List<ReceivingLine> getLines() { return lines; }
    public ReceivingStatus getStatus() { return status; }
    public void setStatus(ReceivingStatus status) { this.status = status; }
    public void addLine(ReceivingLine l) { lines.add(l); }

    @Override
    public String getData() {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIVING:")
          .append(purchaseOrderId).append("|").append(receivedAt).append("|").append(status.name())
          .append("\n");
        for (ReceivingLine l : lines) sb.append(l.getData()).append("\n");
        sb.append("END");
        return sb.toString();
    }

    public static ReceivingRecord parseBlock(List<String> block) {
        if (block.isEmpty() || !block.get(0).startsWith("RECEIVING:")) {
            throw new IllegalArgumentException("Bad receiving block");
        }
        String[] header = block.get(0).substring("RECEIVING:".length()).split("\\|");
        ReceivingRecord rec = new ReceivingRecord(header[0]);
        rec.receivedAt = header[1];
        rec.setStatus(ReceivingStatus.valueOf(header[2]));
        for (int i = 1; i < block.size(); i++) {
            String line = block.get(i);
            if ("END".equals(line)) break;
            if (line.startsWith("RLINE:")) rec.addLine(ReceivingLine.parse(line));
        }
        return rec;
    }
}
