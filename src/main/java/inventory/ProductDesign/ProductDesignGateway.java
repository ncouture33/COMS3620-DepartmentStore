package inventory.ProductDesign;

public interface ProductDesignGateway {

    void lockDocumentation(String path);

    void unlockDocumentation(String path);

    void notifyManager(ProductDesign design);

    void notifyDesignerApproved(ProductDesign design, String managerId);

    void notifyDesignerRejected(ProductDesign design, String managerId);

    void sendToManufacturer(ProductDesign design);
}
