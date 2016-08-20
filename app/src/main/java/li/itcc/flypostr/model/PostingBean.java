package li.itcc.flypostr.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */
@IgnoreExtraProperties
public class PostingBean {
    public String authorId;
    public String title;
    public String text;
    public String imageId;
    public String createdAt;
    public String modifiedAt;
    public String viewCount;
    public String commentCount;
    public Double lat;
    public Double lng;

    public PostingBean() {
    }
}
