package cf.vsing.community.controller;

import cf.vsing.community.entity.Message;
import cf.vsing.community.entity.Page;
import cf.vsing.community.entity.Result;
import cf.vsing.community.entity.User;
import cf.vsing.community.service.InfoService;
import cf.vsing.community.service.MessageService;
import cf.vsing.community.service.UserService;
import cf.vsing.community.util.CommunityUtil;
import cf.vsing.community.util.HostHolderUtil;
import cf.vsing.community.util.StatusUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {
    private final MessageService messageService;
    private final UserService userService;
    private final InfoService infoService;
    private final HostHolderUtil hostHolder;

    @Autowired
    public MessageController(MessageService messageService, UserService userService, InfoService infoService, HostHolderUtil hostHolder) {
        this.messageService = messageService;
        this.userService = userService;
        this.infoService = infoService;
        this.hostHolder = hostHolder;
    }



    @RequestMapping(path = "/message", method = RequestMethod.GET)
    public String messageView(Model model, Page page, HttpServletRequest request) {
        Integer selfId = hostHolder.getUserId(request);

        page.setLimit(10);
        page.setPath("/message");
        page.setRows(messageService.countConversation(selfId));

        List<Message> messages = messageService.getConversation(selfId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                Map<String, Object> conversation = new HashMap<>();
                User target = userService.findById(message.getFromId()==selfId ? message.getToId() : message.getFromId());
                conversation.put("target", target);
                conversation.put("msg", message);
                conversation.put("detailNum", messageService.countDetail(message.getConversationId()));
                conversation.put("unreadNum", messageService.countUnread(selfId, message.getConversationId()));
                conversations.add(conversation);
            }
        }
        model.addAttribute("messageUnreadNum", messageService.countUnread(selfId, null));
        model.addAttribute("infoUnreadNum", infoService.countUnread(selfId, null));
        model.addAttribute("data", conversations);
        return "/site/message";
    }

    @RequestMapping(path = "/message/{target}", method = RequestMethod.GET)
    public String messageDetail(@PathVariable("target") int target, Model model, Page page,HttpServletRequest request) {
        User self = hostHolder.getUser(request);
        User other = userService.findById(target);
        String conversationId = self.getId()<other.getId() ? self.getId() + "_" + other.getId() : other.getId() + "_" + self.getId();

        page.setLimit(10);
        page.setPath("/message/" + target);
        page.setRows(messageService.countDetail(conversationId));


        List<Message> messages = messageService.getDetail(conversationId, page.getOffset(), page.getLimit());
        if (messages != null) {
            List<Integer> unread = new ArrayList<>();
            for (Message message : messages) {
                if (message.getStatus() == 0 && self.getId()==message.getToId()) {
                    unread.add(message.getId());
                }
            }
            if (!unread.isEmpty()) {
                messageService.readMsg(unread);
            }
        }

        model.addAttribute("data", messages);
        model.addAttribute("self", self);
        model.addAttribute("target", other);
        return "/site/message-detail";

    }

    @ResponseBody
    @RequestMapping(path = "/message/send", method = RequestMethod.POST)
    public Result sendMsg(String content, int toId, HttpServletRequest request) {
        if (StringUtils.isBlank(content)) {
            return Result.fail("内容不能为空！");
        }
        Integer selfId = hostHolder.getUserId(request);
        if (selfId==toId) {
            return Result.fail("不能给自己发送消息！");
        }
        if (userService.findById(toId) == null) {
            return Result.fail("回复用户不存在！");
        }

        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setToId(toId);
        message.setConversationId(selfId<toId ? selfId + "_" + toId : toId + "_" + selfId);
        message.setFromId(selfId);
        message.setStatus(0);
        messageService.sendMsg(message);
        return Result.succ("发送成功！");

    }
}
