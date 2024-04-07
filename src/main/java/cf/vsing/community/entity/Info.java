package cf.vsing.community.entity;

import java.util.Date;

//todo:
public class Info {
    private int id;
    private int fromId;
    private int toId;
    private String event;
    private String content;
    private int status;
    private Date createTime;

    public int getId() {
        return id;
    }

    public Info setId(int id) {
        this.id = id;
        return this;
    }

    public int getFromId() {
        return fromId;
    }

    public Info setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public int getToId() {
        return toId;
    }

    public Info setToId(int toId) {
        this.toId = toId;
        return this;
    }

    public String getEvent() {
        return event;
    }

    public Info setEvent(String event) {
        this.event = event;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Info setContent(String content) {
        this.content = content;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Info setStatus(int status) {
        this.status = status;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Info setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    @Override
    public String toString() {
        return "Info{" +
                "id=" + id +
                ", fromId=" + fromId +
                ", toId=" + toId +
                ", event='" + event + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
