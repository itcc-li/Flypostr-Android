package li.itcc.flypostr.poiadd;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiCreateBean {
    private String uuid;
    private double longitude;
    private double latitude;
    private Double exactLongitude;
    private Double exactLatitude;
    private String poiName;
    private String poiDescription;
    private String imageBlobKey;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public String getPoiDescription() {
        return poiDescription;
    }

    public void setPoiDescription(String poiDescription) {
        this.poiDescription = poiDescription;
    }

    public String getImageBlobKey() {
        return imageBlobKey;
    }

    public void setImageBlobKey(String imageBlobKey) {
        this.imageBlobKey = imageBlobKey;
    }

    public Double getExactLongitude() {
        return exactLongitude;
    }

    public void setExactLongitude(Double exactLongitude) {
        this.exactLongitude = exactLongitude;
    }

    public Double getExactLatitude() {
        return exactLatitude;
    }

    public void setExactLatitude(Double exactLatitude) {
        this.exactLatitude = exactLatitude;
    }
}