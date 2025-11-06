package inventory.io;

import Utils.Data;
import inventory.model.Product;
import inventory.model.PurchaseOrder;
import inventory.model.PurchaseOrderLine;
import inventory.model.ReceivingRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InventoryFileStore {
    private final Path baseDir;

    public InventoryFileStore(Path baseDir) { this.baseDir = baseDir; }

    private void appendBlock(Path file, Data data) {
        try {
            Files.createDirectories(file.getParent());
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write(data.getData());
                fw.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> readAllLines(Path file) {
        try {
            if (!Files.exists(file)) return Collections.emptyList();
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path inventoryFile() { return baseDir.resolve("inventory.txt"); }

    public List<Product> loadInventory() {
        List<Product> list = new ArrayList<>();
        for (String line : readAllLines(inventoryFile())) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) list.add(Product.parse(trimmed));
        }
        return list;
    }

    public void saveInventory(List<Product> products) {
        Path file = inventoryFile();
        try {
            Files.createDirectories(file.getParent());
            try (PrintWriter pw = new PrintWriter(new FileWriter(file.toFile(), false))) {
                for (Product p : products) pw.println(p.getData());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path poFile() { return baseDir.resolve("purchase_orders.txt"); }

    public Optional<PurchaseOrder> findPurchaseOrder(String poId) {
        List<String> lines = readAllLines(poFile());
        List<String> block = null;
        for (String line : lines) {
            if (line.startsWith("PO:")) {
                if (block != null && !block.isEmpty()) {
                    if (block.get(0).equals("PO:" + poId)) return Optional.of(PurchaseOrder.parseBlock(block));
                }
                block = new ArrayList<>();
            }
            if (block != null) block.add(line);
            if ("END".equals(line)) {
                if (block.get(0).equals("PO:" + poId)) return Optional.of(PurchaseOrder.parseBlock(block));
                block = null;
            }
        }
        return Optional.empty();
    }

    public Path receivingFile(String poId) { return baseDir.resolve("receivings-" + poId + ".txt"); }
    public void addReceiving(ReceivingRecord rec) { appendBlock(receivingFile(rec.getPurchaseOrderId()), rec); }

    public Path damageFile() { return baseDir.resolve("damage_reports.txt"); }
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
