package cf.vsing.community.entity;

/**
 * 分页信息
 * <p>
 * current  当前页码
 * limit    每页显示元素数量
 * rows     总元素数
 * path     当前页面访问路径
 */
public class Page {
    private int current = 1;
    private int limit = 10;
    private int rows;
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1)
            this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100)
            this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0)
            this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOffset() {
        return current * limit - limit;
    }

    public int getTotal() {
        return (rows % limit == 0) ? (rows / limit) : (rows / limit + 1);
    }

    public int getStart() {
        return Math.max(current - 2, 1);
    }

    public int getEnd() {
        return Math.min(current + 2, getTotal());
    }
}
