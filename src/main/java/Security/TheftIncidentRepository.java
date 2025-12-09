package Security;

import java.io.IOException;

public interface TheftIncidentRepository {
    void save(TheftIncident incident) throws IOException;
}
