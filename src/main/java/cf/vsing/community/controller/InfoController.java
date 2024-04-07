package cf.vsing.community.controller;

import cf.vsing.community.entity.Info;
import cf.vsing.community.entity.Page;
import cf.vsing.community.entity.User;
import cf.vsing.community.service.*;
import cf.vsing.community.util.HostHolderUtil;
import cf.vsing.community.util.StatusUtil;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InfoController {

    private final InfoService infoService;
    private final UserService userService;
    private final CommentService commentService;
    private final ArticleService articleService;
    private final MessageService messageService;
    private final HostHolderUtil hostHolder;
    @Value("${project.path.domain}")
    private String domain;

    public InfoController(InfoService infoService, UserService userService, CommentService commentService, ArticleService articleService, MessageService messageService, HostHolderUtil hostHolder) {
        this.infoService = infoService;
        this.userService = userService;
        this.commentService = commentService;
        this.articleService = articleService;
        this.messageService = messageService;
        this.hostHolder = hostHolder;
    }


    @RequestMapping(path = "/info", method = RequestMethod.GET)
    public String infoView(Model model, Page page, HttpServletRequest request) {
        Integer selfId = hostHolder.getUserId(request);

        page.setLimit(10);
        page.setPath("/info");
        page.setRows(infoService.countEvent(selfId));

        List<Info> infos = infoService.getEvent(selfId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> events = new ArrayList<>();
        if (infos != null) {
            for (Info info : infos) {
                Map<String, Object> event = new HashMap<>();
                JSONObject data = JSONObject.parse(info.getContent());
                switch (info.getEvent()) {
                    case "comment" -> {
                        event.put("name", "评论");
                        event.put("icon", "http://static.nowcoder.com/images/head/reply.png");
                        event.put("content", " : 回复了您");
                    }
                    case "like" -> {
                        event.put("name", "赞");
                        event.put("icon", "http://static.nowcoder.com/images/head/like.png");
                        event.put("content", "　赞了您的文章");
                    }
                    case "follow" -> {
                        event.put("name", "关注");
                        event.put("icon", "http://static.nowcoder.com/images/head/follow.png");
                        event.put("content", "　关注了您");
                    }
                }
                event.put("type", info.getEvent());
                event.put("fromName", userService.findById(info.getFromId()).getName());
                event.put("date", info.getCreateTime());
                event.put("eventDetailNum", infoService.countDetail(selfId, info.getEvent()));
                event.put("eventUnreadNum", infoService.countUnread(selfId, info.getEvent()));
                events.add(event);
            }
        }
        model.addAttribute("messageUnreadNum", messageService.countUnread(selfId, null));
        model.addAttribute("infoUnreadNum", infoService.countUnread(selfId, null));
        model.addAttribute("events", events);
        return "/site/info";
    }

    @RequestMapping(path = "/info/{event}", method = RequestMethod.GET)
    public String infoDetail(@PathVariable("event") String event, Model model, Page page,HttpServletRequest request) {
        User self = hostHolder.getUser(request);
        page.setLimit(10);
        page.setPath("/info/" + event);
        page.setRows(infoService.countDetail(self.getId(), event));


        List<Info> infos = infoService.getDetail(self.getId(), event, page.getOffset(), page.getLimit());
        List<Map<String, Object>> extras = new ArrayList<>();
        if (infos != null) {
            List<Integer> unread = new ArrayList<>();
            for (Info info : infos) {
                JSONObject data = JSONObject.parse(info.getContent());
                Map<String, Object> extra = new HashMap<>();
                extra.put("info", info);
                extra.put("from", userService.findById(info.getFromId()));
                extra.put("date", info.getCreateTime());
                switch (event) {
                    case "comment" -> {
                        extra.put("content", commentService.getById(data.getIntValue("commentId")).getContent());
                        if ( data.getIntValue("targetType") == StatusUtil.ENTITY_ARTICLE) {
                            extra.put("small", "评论了您的文章");
                            extra.put("link", domain + "/article/" + data.get("targetId"));
                        } else {
                            extra.put("small", "回复了您的评论");
                            extra.put("link", null);
                        }
                    }
                    case "like" -> {
                        if (data.getIntValue("targetType") == StatusUtil.ENTITY_ARTICLE) {
                            extra.put("small", "点赞了您的文章");
                            extra.put("content", articleService.findArticleById( data.getIntValue("targetId")).getTitle());
                            extra.put("link", domain + "/article/" + data.get("targetId"));
                        } else if ( data.getIntValue("targetType") == StatusUtil.ENTITY_COMMENT) {
                            extra.put("small", "点赞了您的评论");
                            extra.put("content", commentService.getById(data.getIntValue("targetId")).getContent());
                            extra.put("link", null);
                        } else if ( data.getIntValue("targetType") == StatusUtil.ENTITY_USER) {
                            extra.put("small", "点赞了您的个人主页");
                            extra.put("link", null);
                        }
                    }
                    case "follow" -> extra.put("small", "关注了您");
                }
                extras.add(extra);
                if (info.getStatus() == 0 && self.getId()==info.getToId()) {
                    unread.add(info.getId());
                }
            }
            if (!unread.isEmpty()) {
                infoService.readInfo(unread);
            }
        }

        model.addAttribute("infos", extras);
        model.addAttribute("self", self);
        model.addAttribute("event", event);
        return "/site/info-detail";
    }
}
