package inventory.ProductDesign;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileProductRepository implements ProductRepository {

    private final File file;

    public FileProductRepository(String fileName) {
        this.file = new File(fileName);
    }

    @Override
    public ProductDesign findById(String id) {
        List<ProductDesign> all = readAll();
        for (ProductDesign d : all) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }

    @Override
    public List<ProductDesign> findPendingApproval() {
        List<ProductDesign> pending = new ArrayList<>();
        for (ProductDesign d : readAll()) {
            if (d.getStatus() == ProductStatus.PENDING_APPROVAL) {
                pending.add(d);
            }
        }
        return pending;
    }

    @Override
    public void save(ProductDesign design) {
        // naive implementation: rewrite the whole file
        List<ProductDesign> all = readAll();
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(design.getId())) {
                all.set(i, design);
                updated = true;
                break;
            }
        }
        if (!updated) {
            all.add(design);
        }
        writeAll(all);
    }

    private List<ProductDesign> readAll() {
        List<ProductDesign> result = new ArrayList<>();
        if (!file.exists())
            return result;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty())
                    continue;
                result.add(ProductDesign.fromData(line));
            }
        } catch (Exception e) {
            System.out.println("Error reading product designs: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private void writeAll(List<ProductDesign> designs) {
        try (PrintWriter out = new PrintWriter(new FileWriter(file, false))) {
            for (ProductDesign d : designs) {
                out.println(d.getData());
            }
        } catch (IOException e) {
            System.out.println("Error writing product designs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
