package li.itcc.flypostr.model;

import java.util.Date;

import li.itcc.flypostr.util.ParseHelper;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public class PostingWrapper extends Wrapper<PostingBean> {
    public PostingWrapper() {

    }

    public PostingWrapper(PostingBean bean) {
        super(bean);
    }

    public String getAuthorId() {
        return bean.getAuthorId();
    }

    public void setAuthorId(String authorId) {
        bean.setAuthorId(authorId);
    }

    public String getTitle() {
        return bean.getTitle();
    }

    public void setTitle(String title) {
        bean.setTitle(title);
    }

    public String getText() {
        return bean.getText();
    }

    public void setText(String text) {
        bean.setText(text);
    }

    public String getImageId() {
        return bean.getImageId();
    }

    public void setImageId(String imageId) {
        bean.setImageId(imageId);
    }

    public Date getCreatedAt() {
        return ParseHelper.convertToDate(bean.getCreatedAt());
    }

    public void setCreatedAt(Date createdAt) {
        bean.setCreatedAt(ParseHelper.convertToString(createdAt));
    }

    public Date getModifiedAt() {
        return ParseHelper.convertToDate(bean.getModifiedAt());
    }

    public void setModifiedAt(Date modifiedAt) {
        bean.setModifiedAt(ParseHelper.convertToString(modifiedAt));
    }

    public int getViewCount() {
        return ParseHelper.convertToInteger(bean.getViewCount());
    }

    public void setViewCount(int viewCount) {
        bean.setViewCount(ParseHelper.convertToString(viewCount));
    }

    public int getCommentCount() {
        return ParseHelper.convertToInteger(bean.getCommentCount());
    }

    public void setCommentCount(int commentCount) {
        bean.setCommentCount(ParseHelper.convertToString(commentCount));
    }

    public Double getLat() {
        return bean.getLat();
    }

    public void setLat(Double lat) {
        bean.setLat(lat);
    }

    public Double getLng() {
        return bean.getLng();
    }

    public void setLng(Double lng) {
        bean.setLng(lng);
    }
}
