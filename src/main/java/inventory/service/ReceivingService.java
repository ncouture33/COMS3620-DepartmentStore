package inventory.service;

import inventory.io.InventoryFileStore;
import inventory.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReceivingService {
    private final InventoryFileStore store;

    public ReceivingService(InventoryFileStore store) { this.store = store; }

    public static class CountLine {
        public final String sku;
        public final int receivedQty;
        public final int damagedQty;
        public CountLine(String sku, int receivedQty, int damagedQty) {
            this.sku = sku;
            this.receivedQty = receivedQty;
            this.damagedQty = damagedQty;
        }
    }

    public ReceivingRecord processReceiving(String poId, List<CountLine> receivedLines) {
        Optional<PurchaseOrder> poOpt = store.findPurchaseOrder(poId);
        PurchaseOrder po = poOpt.orElse(null);

        ReceivingRecord rec = new ReceivingRecord(poId);
        boolean discrepancy = false;

        List<String> allSkus = new ArrayList<>();
        if (po != null) {
            for (PurchaseOrderLine l : po.getLines()) {
                if (!contains(allSkus, l.getSku())) allSkus.add(l.getSku());
            }
        }
        for (CountLine cl : receivedLines) {
            if (!contains(allSkus, cl.sku)) allSkus.add(cl.sku);
        }

        for (String sku : allSkus) {
            int expected = expectedForSku(po, sku);
            CountLine cl = countForSku(receivedLines, sku);
            int recQty = cl != null ? cl.receivedQty : 0;
            int dmgQty = cl != null ? cl.damagedQty : 0;

            if (dmgQty > recQty) throw new IllegalArgumentException("Damaged > received for " + sku);
            if (expected != recQty) discrepancy = true;
            if (dmgQty > 0) discrepancy = true;

            rec.addLine(new ReceivingLine(sku, expected, recQty, dmgQty));
        }

        rec.setStatus(discrepancy ? ReceivingStatus.HOLD : ReceivingStatus.CONFIRMED);
        store.addReceiving(rec);
        for (ReceivingLine l : rec.getLines()) {
            if (l.getDamagedQty() > 0) store.appendDamageLine(l.getSku(), poId, l.getDamagedQty());
        }

        List<Product> inv = store.loadInventory();
        for (ReceivingLine l : rec.getLines()) {
            int accepted = l.getAcceptedQty();
            Product p = findProduct(inv, l.getSku());
            if (p == null) {
                p = new Product(l.getSku(), "UNKNOWN", "RECEIVING", 0.0, 0);
                inv.add(p);
            }
            p.setOnHand(p.getOnHand() + accepted);
        }
        store.saveInventory(inv);
        return rec;
    }

    private static boolean contains(List<String> list, String value) {
        for (String s : list) if (s.equals(value)) return true;
        return false;
    }

    private static int expectedForSku(PurchaseOrder po, String sku) {
        if (po == null) return 0;
        for (PurchaseOrderLine l : po.getLines()) if (l.getSku().equals(sku)) return l.getExpectedQty();
        return 0;
    }

    private static CountLine countForSku(List<CountLine> lines, String sku) {
        for (CountLine cl : lines) if (cl.sku.equals(sku)) return cl;
        return null;
    }

    private static Product findProduct(List<Product> inv, String sku) {
        for (Product p : inv) if (p.getSku().equals(sku)) return p;
        return null;
    }
}
