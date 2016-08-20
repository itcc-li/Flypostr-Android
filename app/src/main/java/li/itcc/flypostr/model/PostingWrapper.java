package li.itcc.flypostr.model;

import java.util.Date;

import li.itcc.flypostr.util.ParseHelper;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public class PostingWrapper {
    private static final int SNIPPET_MAX_LENGTH = 50;
    private PostingBean bean;

    public PostingWrapper() {
        this(new PostingBean());
    }

    public PostingBean getBean() {
        return bean;
    }

    public PostingWrapper(PostingBean bean) {
        if (bean == null) {
            throw new NullPointerException();
        }
        this.bean = bean;
    }

    public String getAuthorId() {
        return bean.authorId;
    }

    public void setAuthorId(String authorId) {
        bean.authorId = authorId;
    }

    public String getTitle() {
        return bean.title;
    }

    public void setTitle(String title) {
        bean.title = title;
    }

    public String getText() {
        return bean.text;
    }

    public void setText(String text) {
        bean.text = text;
    }

    public String getImageId() {
        return bean.imageId;
    }

    public void setImageId(String imageId) {
        bean.imageId = imageId;
    }

    public Date getCreatedAt() {
        return ParseHelper.convertToDate(bean.createdAt);
    }

    public void setCreatedAt(Date createdAt) {
        bean.createdAt = ParseHelper.convertToString(createdAt);
    }

    public Date getModifiedAt() {
        return ParseHelper.convertToDate(bean.modifiedAt);
    }

    public void setModifiedAt(Date modifiedAt) {
        bean.modifiedAt = ParseHelper.convertToString(modifiedAt);
    }

    public int getViewCount() {
        return ParseHelper.convertToInteger(bean.viewCount);
    }

    public void setViewCount(Integer viewCount) {
        //bean.viewCount = ParseHelper.convertToLong(viewCount);
    }

    public int getCommentCount() {
        return ParseHelper.convertToInteger(bean.commentCount);
    }

    public void setCommentCount(Integer commentCount) {
        //bean.commentCount = ParseHelper.convertToLong(commentCount);
    }

    public Double getLat() {
        return bean.lat;
    }

    public void setLat(Double lat) {
        bean.lat = lat;
    }

    public Double getLng() {
        return bean.lng;
    }

    public void setLng(Double lng) {
        bean.lng = lng;
    }

    public String getSnippet() {
        String snippet = bean.text;
        if (snippet == null || snippet.length() == 0) {
            return null;
        }
        if (snippet.length() > SNIPPET_MAX_LENGTH) {
            snippet = snippet.substring(0, SNIPPET_MAX_LENGTH - 3) + "...";
        }
        return snippet;
    }
}
