package cf.vsing.community.controller.interceptor;

import cf.vsing.community.service.InfoService;
import cf.vsing.community.service.MessageService;
import cf.vsing.community.util.HostHolderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@SuppressWarnings("NullableProblems")
public class MessageInterceptor implements HandlerInterceptor {
    private final HostHolderUtil hostHolder;
    private final MessageService messageService;
    private final InfoService infoService;
    @Autowired
    MessageInterceptor(HostHolderUtil hostHolder, MessageService messageService, InfoService infoService){
        this.hostHolder = hostHolder;
        this.messageService = messageService;
        this.infoService = infoService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        Integer selfId = hostHolder.getUserId(request);
        if (selfId != null && modelAndView != null) {
            modelAndView.addObject("unreadNum", messageService.countUnread(selfId, null) + infoService.countUnread(selfId, null));
        }
    }
}
