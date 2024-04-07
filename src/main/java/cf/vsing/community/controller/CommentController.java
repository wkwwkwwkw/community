package cf.vsing.community.controller;

import cf.vsing.community.entity.Comment;
import cf.vsing.community.entity.Event;
import cf.vsing.community.entity.User;
import cf.vsing.community.event.EventProducer;
import cf.vsing.community.service.CommentService;
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

import java.util.Arrays;
import java.util.Date;

@Controller
@RequestMapping("/article")
public class CommentController {

    private final CommentService commentService;
    private final HostHolderUtil hostHolder;
    private final EventProducer eventProducer;
    @Autowired
    public CommentController(CommentService commentService, HostHolderUtil hostHolder, EventProducer eventProducer) {
        this.commentService = commentService;
        this.hostHolder = hostHolder;
        this.eventProducer = eventProducer;
    }


    @RequestMapping(path = "/comment/add/{aid}", method = RequestMethod.POST)
    public String addComment(Comment comment,@PathVariable("aid") int aid, int infoId, Model model, HttpServletRequest request) {
        if (StringUtils.isBlank(comment.getContent())) {
            model.addAttribute("errorMsg", "评论不能为空！");
            return "redirect:/article/" + aid;
        }
        Integer selfId = hostHolder.getUserId(request);
        comment.setUserId(selfId);
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        if(!commentService.addComment(comment)){
            model.addAttribute("errorMsg","评论发布失败，请稍后重试！");
            return "redirect:/article/" + aid;
        }

        //触发评论事件
        Event event = new Event()
                .setFromId(selfId)
                .setToId(infoId);
        if(comment.getEntityType() == StatusUtil.ENTITY_ARTICLE){
            event.setEvent(StatusUtil.TOPIC_COMMENT)
                    .setData("targetType", comment.getEntityType())
                    .setData("targetId", comment.getEntityId())
                    .setData("commentId",comment.getId());
        } else {
            event.setEvent(StatusUtil.TOPIC_ARTICLE)
                    .setData("commentCount", 1);
        }
        eventProducer.fireEvent(event);

        return "redirect:/article/" + aid;
    }


}
