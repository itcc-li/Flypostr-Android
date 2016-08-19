package li.itcc.flypostr.poidetail;

import li.itcc.flypostr.backend.PoiOverviewBean;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiDetailBean {
    private PoiOverviewBean overview;
    private String imageUrl;
    private String description;
    private long imageUpdateTime;
    private long imageSize;

    public PoiOverviewBean getOverview() {
        return overview;
    }

    public void setOverview(PoiOverviewBean overview) {
        this.overview = overview;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getImageUpdateTime() {
        return imageUpdateTime;
    }

    public void setImageUpdateTime(long imageUpdateTime) {
        this.imageUpdateTime = imageUpdateTime;
    }

    public long getImageSize() {
        return imageSize;
    }

    public void setImageSize(long imageSize) {
        this.imageSize = imageSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}