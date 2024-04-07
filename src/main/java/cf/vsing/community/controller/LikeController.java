package cf.vsing.community.controller;

import cf.vsing.community.entity.Event;
import cf.vsing.community.entity.Result;
import cf.vsing.community.entity.User;
import cf.vsing.community.event.EventProducer;
import cf.vsing.community.service.LikeService;
import cf.vsing.community.util.CommunityUtil;
import cf.vsing.community.util.HostHolderUtil;
import cf.vsing.community.util.StatusUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {
    private final LikeService likeService;
    private final EventProducer eventProducer;
    private final HostHolderUtil hostHolder;
    @Value("${project.path.domain}")
    private String domain;
    @Autowired
    public LikeController(LikeService likeService, EventProducer eventProducer, HostHolderUtil hostHolder) {
        this.likeService = likeService;
        this.eventProducer = eventProducer;
        this.hostHolder = hostHolder;
    }


    @ResponseBody
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    public Result like(int entityType, int entityId, int userId, HttpServletRequest request) {
        Integer selfId = hostHolder.getUserId(request);

        likeService.like(userId, selfId, entityType, entityId);

        boolean likeStatus = likeService.status(selfId, entityType, entityId);
        long likeCount=likeService.likeCount(entityType, entityId);

        if (likeStatus) {
            Event event = new Event()
                    .setEvent(StatusUtil.TOPIC_LIKE)
                    .setFromId(selfId)
                    .setToId(userId)
                    .setData("targetType", entityType)
                    .setData("targetId", entityId);
            eventProducer.fireEvent(event);
        }
        return Result.succ("点赞成功",Map.of( "likeCount",likeCount,"likeStatus",likeStatus));
    }
}
