package cf.vsing.community.util;

import cf.vsing.community.controller.advice.ExceptionAdvice;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CookieUtil {
    private static final Logger log = LoggerFactory.getLogger(CookieUtil.class);
    public static String getCookie(HttpServletRequest request, String name) {
        if (request == null || name == null||request.getCookies()==null) {
            log.warn("Cookie为空或参数非法");
        } else {
            for (Cookie value : request.getCookies()) {
                if (value.getName().equals(name)) {
                    return value.getValue();
                }
            }
        }
        return null;
    }
}
