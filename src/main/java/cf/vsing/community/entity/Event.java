package cf.vsing.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private String event;
    private int fromId;
    private int toId;
    private int level;
    private final Map<String, Object> data = new HashMap<>();

    public String getEvent() {
        return event;
    }

    public Event setEvent(String event) {
        this.event = event;
        return this;
    }

    public int getFromId() {
        return fromId;
    }

    public Event setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public int getToId() {
        return toId;
    }

    public Event setToId(int toId) {
        this.toId = toId;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public Event setLevel(int level) {
        this.level = level;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
