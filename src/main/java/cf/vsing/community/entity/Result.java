package cf.vsing.community.entity;

import java.io.Serializable;

public class Result implements Serializable {

    private Integer code;
    private String msg;
    private Object data;

    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Result succ(String msg) {
        return new Result(2000, msg, null);
    }
    public static Result succ(Object data) {
        return new Result(2000, "操作成功", data);
    }
    public static Result succ(String msg,Object data) {
        return new Result(2000, msg, data);
    }
    public static Result succ(ResultCode code) {
        return new Result(code.getCode(), code.getMessage(), null);
    }
    public static Result succ(ResultCode code,Object data) {
        return new Result(code.getCode(), code.getMessage(), data);
    }


    public static Result fail(String msg) {
        return new Result(4000, msg, null);
    }
    public static Result fail(String msg,Object data) {
        return new Result(4000, msg, data);
    }
    public static Result fail(ResultCode code) {
        return new Result(code.getCode(), code.getMessage(), null);
    }
    public static Result fail(ResultCode code,Object data) {
        return new Result(code.getCode(), code.getMessage(), data);
    }

    public static Result err(String msg) {
        return new Result(5000, msg, null);
    }
    public static Result err(ResultCode code) {
        return new Result(code.getCode(), code.getMessage(), null);
    }
    public static Result err(ResultCode code,Object data) {
        return new Result(code.getCode(), code.getMessage(), data);
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }
}
