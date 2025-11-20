package inventory.model;

import Utils.Data;

public class Product implements Data {
    private final String sku;
    private String name;
    private String location;
    private double unitPrice;
    private int onHand;

    public Product(String sku, String name, String location, double unitPrice, int onHand) {
        this.sku = sku;
        this.name = name;
        this.location = location;
        this.unitPrice = unitPrice;
        this.onHand = onHand;
    }

    public Product(String sku, String name, String location, double unitPrice) {
        this(sku, name, location, unitPrice, 0);
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getOnHand() {
        return onHand;
    }

    public void setOnHand(int onHand) {
        this.onHand = onHand;
    }

    @Override
    public String getData() {
        return String.join("|",
                sku,
                name.replace("|", "/"),
                location.replace("|", "/"),
                String.valueOf(unitPrice),
                String.valueOf(onHand));
    }

    public static Product parse(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 5)
            throw new IllegalArgumentException("Bad product: " + line);

        return new Product(
                parts[0],
                parts[1],
                parts[2],
                Double.parseDouble(parts[3]),
                Integer.parseInt(parts[4]));
    }
}
