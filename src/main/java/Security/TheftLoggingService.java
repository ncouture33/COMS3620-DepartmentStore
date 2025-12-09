package Security;

public class TheftLoggingService {

    private final TheftIncidentRepository repository;

    public TheftLoggingService(TheftIncidentRepository repository) {
        this.repository = repository;
    }

    public void logTheft(String dateTime,
            String location,
            String stolenItemsDescription,
            boolean hasCameraFootage,
            boolean hasWitnesses,
            String policeReportNumber) {

        String incidentId = generateIncidentId(location);

        TheftIncident incident = new TheftIncident(
                incidentId,
                dateTime,
                location,
                stolenItemsDescription,
                hasCameraFootage,
                hasWitnesses,
                policeReportNumber);

        try {
            repository.save(incident);
        } catch (Exception e) {
            throw new RuntimeException("Unable to save theft incident", e);
        }
    }

    private String generateIncidentId(String location) {
        long now = System.currentTimeMillis();
        int locHash = location == null ? 0 : Math.abs(location.hashCode());
        return "THEFT-" + now + "-" + locHash;
    }
}
