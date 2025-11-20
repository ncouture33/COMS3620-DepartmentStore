package inventory.ProductDesign;

import java.util.Objects;

public class ProductApprovalService {

    private final ProductRepository repository;
    private final NotificationService notificationService;
    private final ManufacturingGateway manufacturingGateway;
    private final DocumentationLockService documentationLockService;

    public ProductApprovalService(ProductRepository repository,
            NotificationService notificationService,
            ManufacturingGateway manufacturingGateway,
            DocumentationLockService documentationLockService) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.manufacturingGateway = manufacturingGateway;
        this.documentationLockService = documentationLockService;
    }

    public void submitForApproval(String productId) {
        ProductDesign design = requireDesign(productId);
        if (design.getStatus() != ProductStatus.DRAFT &&
                design.getStatus() != ProductStatus.REJECTED) {
            throw new IllegalStateException("Only DRAFT or REJECTED designs can be submitted.");
        }

        documentationLockService.lock(design.getDocumentationPath());
        design.setLocked(true);

        design.setStatus(ProductStatus.PENDING_APPROVAL);
        repository.save(design);

        notificationService.notifyManagerNewProductReady(design);
    }

    public void approveProduct(String productId, String managerId) {
        ProductDesign design = requireDesign(productId);

        if (design.getStatus() != ProductStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Design is not pending approval.");
        }

        design.setStatus(ProductStatus.APPROVED);
        documentationLockService.lock(design.getDocumentationPath());
        design.setLocked(true);

        repository.save(design);

        notificationService.notifyDesignerApproved(design, managerId);

        manufacturingGateway.sendToManufacturer(design);
    }

    public void rejectProduct(String productId, String managerId, String comment) {
        ProductDesign design = requireDesign(productId);

        if (design.getStatus() != ProductStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Design is not pending approval.");
        }

        design.setStatus(ProductStatus.REJECTED);
        design.setManagerComment(Objects.requireNonNullElse(comment, ""));

        documentationLockService.unlock(design.getDocumentationPath());
        design.setLocked(false);

        repository.save(design);

        notificationService.notifyDesignerRejected(design, managerId);
    }

    // ---------- Helper ----------

    private ProductDesign requireDesign(String productId) {
        ProductDesign design = repository.findById(productId);
        if (design == null) {
            throw new IllegalArgumentException("Product design not found: " + productId);
        }
        return design;
    }

    public interface NotificationService {
        void notifyManagerNewProductReady(ProductDesign design);

        void notifyDesignerApproved(ProductDesign design, String managerId);

        void notifyDesignerRejected(ProductDesign design, String managerId);
    }

    public interface ManufacturingGateway {
        void sendToManufacturer(ProductDesign design);
    }

    public interface DocumentationLockService {
        void lock(String documentationPath);

        void unlock(String documentationPath);
    }
}
