package Security;

import Utils.Data;

public class TheftIncident implements Data {

    private final String incidentId;
    private final String dateTime;
    private final String location;
    private final String stolenItems;
    private final boolean hasCameraFootage;
    private final boolean hasWitnesses;
    private final String policeReportNumber;

    public TheftIncident(String incidentId,
            String dateTime,
            String location,
            String stolenItems,
            boolean hasCameraFootage,
            boolean hasWitnesses,
            String policeReportNumber) {
        this.incidentId = incidentId;
        this.dateTime = dateTime;
        this.location = location;
        this.stolenItems = stolenItems;
        this.hasCameraFootage = hasCameraFootage;
        this.hasWitnesses = hasWitnesses;
        this.policeReportNumber = policeReportNumber;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getLocation() {
        return location;
    }

    public String getStolenItems() {
        return stolenItems;
    }

    public boolean hasCameraFootage() {
        return hasCameraFootage;
    }

    public boolean hasWitnesses() {
        return hasWitnesses;
    }

    public String getPoliceReportNumber() {
        return policeReportNumber;
    }

    private String sanitize(String value) {
        if (value == null)
            return "";
        return value.replace("|", "/");
    }

    @Override
    public String getData() {
        return incidentId + "|"
                + sanitize(dateTime) + "|"
                + sanitize(location) + "|"
                + sanitize(stolenItems) + "|"
                + hasCameraFootage + "|"
                + hasWitnesses + "|"
                + sanitize(policeReportNumber) + "|";
    }

    public static TheftIncident fromData(String line) {
        String[] parts = line.split("\\|", -1);
        String incidentId = parts[0];
        String dateTime = parts[1];
        String location = parts[2];
        String stolenItems = parts[3];
        boolean hasCameraFootage = Boolean.parseBoolean(parts[4]);
        boolean hasWitnesses = Boolean.parseBoolean(parts[5]);
        String policeReportNumber = parts[6];

        return new TheftIncident(
                incidentId,
                dateTime,
                location,
                stolenItems,
                hasCameraFootage,
                hasWitnesses,
                policeReportNumber);
    }
}
