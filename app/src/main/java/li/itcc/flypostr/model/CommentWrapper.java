package li.itcc.flypostr.model;

import java.util.Date;

import li.itcc.flypostr.util.FormatHelper;

/**
 * Created by Arthur on 21.08.2016.
 */

public class CommentWrapper {
    private CommentBean bean;

    public CommentWrapper() {
        this(new CommentBean());
    }

    public CommentWrapper(CommentBean bean) {
        if (bean == null) {
            throw new NullPointerException();
        }
        this.bean = bean;
    }

    public CommentBean getBean() {
        return bean;
    }

    //// accessors

    public String getAuthorId() {
        return bean.authorId;
    }

    public void setAuthorId(String authorId) {
        bean.authorId = authorId;
    }

    public Date getCreatedAt() {
        return FormatHelper.convertToDate(bean.createdAt);
    }

    public void setCreatedAt(Date createdAt) {
        bean.createdAt = FormatHelper.convertToString(createdAt);
    }

    public String getText() {
        return bean.text;
    }

    public void setText(String text) {
        bean.text = text;
    }

}
