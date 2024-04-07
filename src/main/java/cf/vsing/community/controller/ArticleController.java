package cf.vsing.community.controller;

import cf.vsing.community.entity.*;
import cf.vsing.community.event.EventProducer;
import cf.vsing.community.service.*;
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
@RequestMapping("/article")
public class ArticleController implements StatusUtil {
    private final ArticleService articleService;
    private final UserService userService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final SearchService searchService;
    private final HostHolderUtil hostHolder;
    private final EventProducer eventProducer;

    @Autowired
    ArticleController(ArticleService articleService, UserService userService, CommentService commentService, LikeService likeService, SearchService searchService, HostHolderUtil hostHolder, EventProducer eventProducer){
        this.articleService = articleService;
        this.userService = userService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.searchService = searchService;
        this.hostHolder = hostHolder;
        this.eventProducer = eventProducer;
    }

    @ResponseBody
    @RequestMapping(path = "/publish", method = RequestMethod.POST)
    public Result publish(String title, String content, HttpServletRequest request) {
        Integer selfId = hostHolder.getUserId(request);
        if (StringUtils.isBlank(title)) {
            return Result.fail("文章标题不能为空！");
        } else if (StringUtils.isBlank(content)) {
            return Result.fail("文章内容不能为空！");
        }

        Article article = new Article()
                .setUserId(selfId)
                .setTitle(title)
                .setContent(content)
                .setCreateTime(new Date());

        articleService.addArticle(article);

        Event event = new Event()
                .setEvent(StatusUtil.TOPIC_ARTICLE)
                .setFromId(selfId)
                .setToId(StatusUtil.SYSTEM_INFO)
                .setData("article", article);
        eventProducer.fireEvent(event);

        return Result.succ("文章发布成功！",article.getId());
    }

    @RequestMapping(path = "/{aid}", method = RequestMethod.GET)
    public String articleDetail(@PathVariable("aid") int aid, Model model, Page page,HttpServletRequest request) {
        Integer selfId = hostHolder.getUserId(request);
        Article article = articleService.findArticleById(aid);

        page.setLimit(10);
        page.setPath("/article/" + aid);
        page.setRows(article.getCommentCount());

        User author = userService.findById(article.getUserId());
        List<Comment> comments = commentService.getByEntityId(StatusUtil.ENTITY_ARTICLE, aid, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentsVO = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> commentVO = new HashMap<>();
                List<Comment> replies = commentService.getByEntityId(StatusUtil.ENTITY_COMMENT, comment.getId(), 0, StatusUtil.REPLAY_SHOW_NUMBER);
                List<Map<String, Object>> repliesVO = new ArrayList<>();
                if (replies != null) {
                    for (Comment reply : replies) {
                        Map<String, Object> replyVO = new HashMap<>();
                        replyVO.put("likeCount", likeService.likeCount(StatusUtil.ENTITY_COMMENT, reply.getId()));
                        replyVO.put("likeStatus", selfId != null && likeService.status(selfId, StatusUtil.ENTITY_COMMENT, reply.getId()));
                        replyVO.put("user", userService.findById(reply.getUserId()));
                        replyVO.put("reply", reply);
                        User target =userService.findById(reply.getTargetId());
                        replyVO.put("target", target);
                        repliesVO.add(replyVO);
                    }
                }
                commentVO.put("likeCount", likeService.likeCount(StatusUtil.ENTITY_COMMENT, comment.getId()));
                commentVO.put("likeStatus", selfId != null && likeService.status(selfId, StatusUtil.ENTITY_COMMENT, comment.getId()));
                commentVO.put("user", userService.findById(comment.getUserId()));
                commentVO.put("comment", comment);
                commentVO.put("replies", repliesVO);
                commentVO.put("replyCount", commentService.countByEntityId(StatusUtil.ENTITY_COMMENT, comment.getId()));
                commentsVO.add(commentVO);
            }
        }
        model.addAttribute("likeCount", likeService.likeCount(StatusUtil.ENTITY_ARTICLE, article.getId()));
        model.addAttribute("likeStatus", selfId != null && likeService.status(selfId, StatusUtil.ENTITY_ARTICLE, article.getId()));
        model.addAttribute("ENTITY_TYPE_ARTICLE", StatusUtil.ENTITY_ARTICLE);
        model.addAttribute("ENTITY_TYPE_COMMENT", StatusUtil.ENTITY_COMMENT);
        model.addAttribute("comments", commentsVO);
        model.addAttribute("article", article);
        model.addAttribute("user", author);
        return "/site/article";
    }

    @ResponseBody
    @RequestMapping(path = "/top/{aid}",method = RequestMethod.POST)
    public Result setTop(@PathVariable(name = "aid") int aid,boolean status,HttpServletRequest request){
        if(articleService.updateType(aid,status?0:1)){

            Event event = new Event()
                    .setEvent(StatusUtil.TOPIC_ARTICLE)
                    .setFromId(hostHolder.getUserId(request))
                    .setToId(StatusUtil.SYSTEM_INFO)
                    .setData("type", status?0:1);
            eventProducer.fireEvent(event);
            return Result.succ(status?"取消置顶成功":"置顶成功");
        }
        return Result.succ("文章不存在");
    }

    @ResponseBody
    @RequestMapping(path = "/important/{aid}",method = RequestMethod.POST)
    public Result setImportant(@PathVariable(name = "aid") int aid,boolean status,HttpServletRequest request){
        if(articleService.updateStatus(aid,status?0:1)){

            Event event = new Event()
                    .setEvent(StatusUtil.TOPIC_ARTICLE)
                    .setFromId(hostHolder.getUserId(request))
                    .setToId(StatusUtil.SYSTEM_INFO)
                    .setData("type", status?0:1);
            eventProducer.fireEvent(event);
            return Result.succ(status?"取消加精成功":"加精成功");
        }
        return Result.succ("文章不存在");
    }

    @ResponseBody
    @RequestMapping(path = "/delete/{aid}",method = RequestMethod.POST)
    public Result delete(@PathVariable(name = "aid") int aid,HttpServletRequest request){
        if(articleService.updateType(aid,1)){
            searchService.delete(String.valueOf(aid));
            return Result.succ("置顶成功");
        }
        return Result.succ("文章不存在");
    }
}
