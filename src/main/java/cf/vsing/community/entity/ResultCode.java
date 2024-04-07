package cf.vsing.community.entity;

public enum ResultCode {

    SUCCESS(2000, "成功"),
    FAILURE(4000, "失败"),
    ERROR(5000, "服务器错误"),

    SUCCESS_ADD(2001, "添加成功"),
    SUCCESS_UPDATE(2002, "更新成功"),
    SUCCESS_DELETE(2003, "删除成功"),


    PARAM_INVALID(4001, "参数无效"),
    PARAM_BLANK(4002, "参数为空"),
    AUTH_NOT_LOGIN(4011, "用户未登录"),
    AUTH_DENIED(4012, "用户权限不足"),

    NOT_FOUND_RESOURCE(4040, "资源未找到"),
    NOT_FOUND_PATH(4041, "请求路径无效"),
    NOT_FOUND_DELETED(4100, "资源已删除");


    private Integer code;
    private String message;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
