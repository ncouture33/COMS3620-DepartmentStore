package inventory.io;

import Utils.Data;
import inventory.model.Product;
import inventory.model.PurchaseOrder;
import inventory.model.ReceivingRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryFileStore {
    private final Path baseDir;

    public InventoryFileStore(Path baseDir) {
        this.baseDir = baseDir;
    }

    // ----------------- Shared helpers -----------------

    private void appendBlock(Path file, Data data) {
        try {
            Files.createDirectories(file.getParent());
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write(data.getData());
                fw.write("\n"); // separate blocks
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> readAllLines(Path file) {
        try {
            if (!Files.exists(file))
                return Collections.emptyList();
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------- Inventory (inventory.txt) -----------------

    public Path inventoryFile() {
        return baseDir.resolve("inventory.txt");
    }

    public Map<String, Product> loadInventory() {
        Map<String, Product> map = new LinkedHashMap<>();
        for (String line : readAllLines(inventoryFile())) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                Product p = Product.parse(trimmed);
                map.put(p.getSku(), p);
            }
        }
        return map;
    }

    public void saveInventory(Map<String, Product> products) {
        Path file = inventoryFile();
        try {
            Files.createDirectories(file.getParent());
            try (PrintWriter pw = new PrintWriter(new FileWriter(file.toFile(), false))) {
                for (Product p : products.values()) {
                    pw.println(p.getData());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------- Product catalog (products.txt) -----------------

    public Path productFile() {
        return baseDir.resolve("products.txt");
    }

    /**
     * Load product master data from products.txt.
     * Used for looking up product info & unit cost when creating POs.
     */
    public Map<String, Product> loadProductCatalog() {
        Map<String, Product> map = new LinkedHashMap<>();
        for (String line : readAllLines(productFile())) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                Product p = Product.parse(trimmed);
                map.put(p.getSku(), p);
            }
        }
        return map;
    }

    // ----------------- Purchase Orders (purchase_orders.txt) -----------------

    public Path poFile() {
        return baseDir.resolve("purchase_orders.txt");
    }

    /**
     * Load all purchase orders from purchase_orders.txt.
     * Each PO is stored as a block:
     *
     * PO:<id>|<status>
     * ITEM:...
     * ...
     * END
     */
    public List<PurchaseOrder> loadAllPurchaseOrders() {
        List<String> lines = readAllLines(poFile());
        List<PurchaseOrder> result = new ArrayList<>();

        List<String> block = null;
        for (String line : lines) {
            if (line.startsWith("PO:")) {
                // starting a new block
                if (block != null && !block.isEmpty()) {
                    result.add(PurchaseOrder.parseBlock(block));
                }
                block = new ArrayList<>();
            }
            if (block != null) {
                block.add(line);
            }
            if ("END".equals(line) && block != null) {
                result.add(PurchaseOrder.parseBlock(block));
                block = null;
            }
        }

        return result;
    }

    /**
     * Overwrite purchase_orders.txt with the given list of POs.
     */
    public void saveAllPurchaseOrders(List<PurchaseOrder> orders) {
        Path file = poFile();
        try {
            Files.createDirectories(file.getParent());
            try (PrintWriter pw = new PrintWriter(new FileWriter(file.toFile(), false))) {
                boolean first = true;
                for (PurchaseOrder po : orders) {
                    if (!first) {
                        pw.println(); // blank line between blocks (optional)
                    }
                    pw.print(po.getData());
                    first = false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a single PO by id.
     */
    public Optional<PurchaseOrder> findPurchaseOrder(String poId) {
        for (PurchaseOrder po : loadAllPurchaseOrders()) {
            if (po.getId().equals(poId)) {
                return Optional.of(po);
            }
        }
        return Optional.empty();
    }

    /**
     * Append a new PO at the end of the file.
     */
    public void addPurchaseOrder(PurchaseOrder po) {
        List<PurchaseOrder> all = loadAllPurchaseOrders();
        all.add(po);
        saveAllPurchaseOrders(all);
    }

    /**
     * Replace an existing PO with the same id, or add if not present.
     * Used for editing or canceling POs.
     */
    public void upsertPurchaseOrder(PurchaseOrder po) {
        List<PurchaseOrder> all = loadAllPurchaseOrders();
        boolean replaced = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(po.getId())) {
                all.set(i, po);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            all.add(po);
        }
        saveAllPurchaseOrders(all);
    }

    // ----------------- Receivings (receivings-<poId>.txt) -----------------

    public Path receivingFile(String poId) {
        return baseDir.resolve("receivings-" + poId + ".txt");
    }

    public void addReceiving(ReceivingRecord rec) {
        appendBlock(receivingFile(rec.getPurchaseOrderId()), rec);
    }

    // ----------------- Damage reports (damage_reports.txt) -----------------

    public Path damageFile() {
        return baseDir.resolve("damage_reports.txt");
    }

    public void appendDamageLine(String sku, String poId, int damagedQty) {
        String line = String.join("|", sku, poId, String.valueOf(damagedQty));
        appendLine(damageFile(), line);
    }

    private void appendLine(Path file, String line) {
        try {
            Files.createDirectories(file.getParent());
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write(line);
                fw.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
