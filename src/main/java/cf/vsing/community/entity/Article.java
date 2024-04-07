package cf.vsing.community.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {

    private int id;
    private int userId;
    private String title;
    private String content;
    private int type;
    private int status;
    private Date createTime;
    private int commentCount;
    private double score;

    public int getId() {
        return this.id;
    }

    public Article setId(int id) {
        this.id = id;
        return this;
    }

    public int getUserId() {
        return this.userId;
    }

    public Article setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Article setContent(String content) {
        this.content = content;
        return this;
    }

    public int getType() {
        return type;
    }

    public Article setType(int type) {
        this.type = type;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Article setStatus(int status) {
        this.status = status;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Article setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public Article setCommentCount(int commentCount) {
        this.commentCount = commentCount;
        return this;
    }

    public double getScore() {
        return score;
    }

    public Article setScore(double score) {
        this.score = score;
        return this;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createTime=" + createTime +
                ", commentCount=" + commentCount +
                ", score=" + score +
                '}';
    }
}
