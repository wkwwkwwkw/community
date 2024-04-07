package cf.vsing.community.controller;

import cf.vsing.community.entity.Article;
import cf.vsing.community.entity.Page;
import cf.vsing.community.entity.User;
import cf.vsing.community.service.ArticleService;
import cf.vsing.community.service.LikeService;
import cf.vsing.community.service.UserService;
import cf.vsing.community.util.HostHolderUtil;
import cf.vsing.community.util.StatusUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    private final ArticleService articleService;
    private final UserService userService;
    private final LikeService likeService;
    private final HostHolderUtil hostHolder;
    @Autowired
    public IndexController(ArticleService articleService, UserService userService, LikeService likeService, HostHolderUtil hostHolder) {
        this.articleService = articleService;
        this.userService = userService;
        this.likeService = likeService;
        this.hostHolder = hostHolder;
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, HttpServletRequest request) {
        Integer selfId = hostHolder.getUserId(request);
        //设置页码相关参数
        //page 可在模板中直接访问
        page.setRows(articleService.countArticle(0));
        page.setPath("/");

        //获取文章信息列表
        List<Article> articles = articleService.findArticleByUserId(0, page.getOffset(), page.getLimit());

        //轮询查询文章作者信息
        List<Map<String, Object>> articlesVO = new ArrayList<>();
        if (articles != null) {
            for (Article article : articles) {
                Map<String, Object> articleVO = new HashMap<>();
                User user = userService.findById(article.getUserId());
                articleVO.put("likeCount", likeService.likeCount(StatusUtil.ENTITY_ARTICLE, article.getId()));
                articleVO.put("likeStatus", selfId != null && likeService.status(selfId, StatusUtil.ENTITY_ARTICLE, article.getId()));
                articleVO.put("article", article);
                articleVO.put("user", user);
                articlesVO.add(articleVO);
            }
        }

        //传入model
        model.addAttribute("articles", articlesVO);

        //指定视图
        return "/index";
    }
}
