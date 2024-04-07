package cf.vsing.community.controller.interceptor;

import cf.vsing.community.entity.User;
import cf.vsing.community.util.HostHolderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Component
@SuppressWarnings("NullableProblems")
public class HostHolderInterceptor implements HandlerInterceptor {
    private final HostHolderUtil hostHolder;
    @Autowired
    HostHolderInterceptor(HostHolderUtil hostHolder){
        this.hostHolder = hostHolder;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        User user = hostHolder.getUser(request);
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }
}
