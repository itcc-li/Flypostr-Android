package li.itcc.flypostr.services;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiOverviewQuery {
    /**
     * Current user location
     */
    private double latitude;
    /**
     * Current user location
     */
    private double longitude;

    private int maxCount;


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }


}