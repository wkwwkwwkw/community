package cf.vsing.community.controller;

import cf.vsing.community.entity.*;
import cf.vsing.community.event.EventProducer;
import cf.vsing.community.service.FollowService;
import cf.vsing.community.service.UserService;
import cf.vsing.community.util.CommunityUtil;
import cf.vsing.community.util.HostHolderUtil;
import cf.vsing.community.util.StatusUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements StatusUtil {
    private final FollowService followService;
    private final UserService userService;
    private final EventProducer eventProducer;
    private final HostHolderUtil hostHolder;
    @Autowired
    FollowController(FollowService followService, UserService userService, EventProducer eventProducer, HostHolderUtil hostHolder){
        this.followService = followService;
        this.userService = userService;
        this.eventProducer = eventProducer;
        this.hostHolder = hostHolder;
    }
    @ResponseBody
    @RequestMapping(path = "/user/follow", method = RequestMethod.POST)
    public Result follow(int entityType, int entityId, int authorId, HttpServletRequest request) {
        //类型判断
        if (entityType != ENTITY_USER && entityType != ENTITY_ARTICLE) {
            throw new IllegalArgumentException("[关注]不存在的实体类型");
        }

        Integer selfId = hostHolder.getUserId(request);

        if (selfId == null) {
            return Result.fail("请先登录！");
        } else if (entityType == ENTITY_USER && selfId==entityId) {
            return Result.fail("不能关注自己！");
        }

        boolean status=followService.followStatus(selfId, entityType, entityId);
        if (status) {
            followService.unfollow(selfId, entityType, entityId);
            return Result.succ("取关成功！",false);
        } else {
            followService.follow(selfId, entityType, entityId);

            Event event = new Event()
                    .setEvent(StatusUtil.TOPIC_FOLLOW)
                    .setFromId(selfId)
                    .setToId(authorId)
                    .setData("targetType", entityType)
                    .setData("targetId", entityId);
            eventProducer.fireEvent(event);
            return Result.succ("关注成功！",true);
        }
    }

    @RequestMapping(value = "/user/followee/{userId}", method = RequestMethod.GET)
    public String followee(@PathVariable("userId") int userId, Page page, Model model, HttpServletRequest request) {
        Integer selfId = hostHolder.getUserId(request);
        User user = userService.findById(userId);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        boolean isLogin = selfId != null;
        boolean isSelf = isLogin && selfId==userId;

        page.setLimit(5);
        page.setPath("/user/followee/" + userId);
        page.setRows((int) followService.followeeCount(ENTITY_USER, userId));

        List<Map<String, Object>> data = followService.findFollowees(userId,ENTITY_USER, page.getOffset(), page.getLimit());
        if (data != null) {
            for (Map<String, Object> map : data) {
                int id = ((User) map.get("user")).getId();
                map.put("followStatus", isLogin && followService.followStatus(selfId, ENTITY_USER, id));
            }
        }

        model.addAttribute("type", FIND_TYPE_FOLLOWEE);
        model.addAttribute("user", user);
        model.addAttribute("data", data);
        model.addAttribute("isLogin", isLogin);
        model.addAttribute("isSelf", isSelf);
        return "/site/follow";
    }


    @RequestMapping(value = "/user/follower/{userId}", method = RequestMethod.GET)
    public String follower(@PathVariable("userId") int userId, Page page, Model model, HttpServletRequest request) {
        Integer selfId = hostHolder.getUserId(request);
        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        boolean isLogin = selfId != null;
        boolean isSelf = isLogin && selfId==userId;

        page.setLimit(5);
        page.setPath("/user/follower/" + userId);
        page.setRows((int) followService.followerCount(ENTITY_USER, userId));

        List<Map<String, Object>> data = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (data != null) {
            for (Map<String, Object> map : data) {
                int id = ((User) map.get("user")).getId();
                map.put("followStatus", isLogin && followService.followStatus(selfId, ENTITY_USER, id));
            }
        }

        model.addAttribute("type", FIND_TYPE_FOLLOWER);
        model.addAttribute("user", user);
        model.addAttribute("data", data);
        model.addAttribute("isLogin", isLogin);
        model.addAttribute("isSelf", isSelf);
        return "/site/follow";
    }
}
