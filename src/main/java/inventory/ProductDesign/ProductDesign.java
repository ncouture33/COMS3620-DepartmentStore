package inventory.ProductDesign;

import Utils.Data;

//Represents a new product design proposal in the design system.

public class ProductDesign implements Data {

    private final String id;
    private String name;
    private String designerId;
    private ProductStatus status;
    private String documentationPath;
    private boolean locked;
    private String managerComment;

    public ProductDesign(String id,
            String name,
            String designerId,
            String documentationPath) {
        this.id = id;
        this.name = name;
        this.designerId = designerId;
        this.documentationPath = documentationPath;
        this.status = ProductStatus.DRAFT;
        this.locked = false;
        this.managerComment = "";
    }

    // --- Getters / setters ---

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesignerId() {
        return designerId;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getDocumentationPath() {
        return documentationPath;
    }

    public void setDocumentationPath(String documentationPath) {
        this.documentationPath = documentationPath;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getManagerComment() {
        return managerComment;
    }

    public void setManagerComment(String managerComment) {
        this.managerComment = managerComment;
    }

    @Override
    public String getData() {
        return id + "|" +
                name + "|" +
                designerId + "|" +
                status.name() + "|" +
                locked + "|" +
                documentationPath + "|" +
                managerComment.replace("|", "/");
    }

    public static ProductDesign fromData(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid product design line: " + line);
        }
        ProductDesign design = new ProductDesign(
                parts[0],
                parts[1],
                parts[2],
                parts[5]);
        design.setStatus(ProductStatus.valueOf(parts[3]));
        design.setLocked(Boolean.parseBoolean(parts[4]));
        design.setManagerComment(parts[6]);
        return design;
    }
}
