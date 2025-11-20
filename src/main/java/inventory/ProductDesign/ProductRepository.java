package inventory.ProductDesign;

import java.util.List;

public interface ProductRepository {

    ProductDesign findById(String id);

    List<ProductDesign> findPendingApproval();

    void save(ProductDesign design);
}
