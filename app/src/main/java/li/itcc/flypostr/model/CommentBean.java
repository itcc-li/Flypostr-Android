package li.itcc.flypostr.model;

import java.util.Date;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public class CommentBean {
    private String authorId;
    private Date createdAt;
    private String text;

    public CommentBean() {

    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
