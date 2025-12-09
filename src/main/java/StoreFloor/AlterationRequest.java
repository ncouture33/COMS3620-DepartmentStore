package StoreFloor;

import Utils.Data;

public class AlterationRequest implements Data {
    private String trackingNumber;
    private String customerName;
    private String customerPhone;
    private String itemSKU;
    private String purchaseDate;
    private String alterationInstructions;
    private String measurements;
    private double cost;
    private String completionDate;
    private String status;

    public AlterationRequest(String trackingNumber, String customerName, String customerPhone,
                            String itemSKU, String purchaseDate,
                            String alterationInstructions, String measurements,
                            double cost, String completionDate, String status) {
        this.trackingNumber = trackingNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.itemSKU = itemSKU;
        this.purchaseDate = purchaseDate;
        this.alterationInstructions = alterationInstructions;
        this.measurements = measurements;
        this.cost = cost;
        this.completionDate = completionDate;
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getItemSKU() {
        return itemSKU;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public String getAlterationInstructions() {
        return alterationInstructions;
    }

    public String getMeasurements() {
        return measurements;
    }

    public double getCost() {
        return cost;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String sanitize(String value) {
        if (value == null)
            return "";
        return value.replace("|", "/");
    }

    @Override
    public String getData() {
        return trackingNumber + "|" + 
               sanitize(customerName) + "|" + 
               sanitize(customerPhone) + "|" + 
               itemSKU + "|" + 
               sanitize(purchaseDate) + "|" + 
               sanitize(alterationInstructions) + "|" + 
               sanitize(measurements) + "|" + 
               cost + "|" + 
               sanitize(completionDate) + "|" + 
               status;
    }
}
