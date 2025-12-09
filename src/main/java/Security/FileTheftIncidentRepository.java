package Security;

import java.io.FileWriter;
import java.io.IOException;

public class FileTheftIncidentRepository implements TheftIncidentRepository {

    private final String fileName;

    public FileTheftIncidentRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void save(TheftIncident incident) throws IOException {
        try (FileWriter fw = new FileWriter(fileName, true)) {
            fw.write(incident.getData());
            fw.write(System.lineSeparator());
        }
    }
}
