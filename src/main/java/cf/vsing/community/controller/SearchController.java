package cf.vsing.community.controller;

import cf.vsing.community.entity.Article;
import cf.vsing.community.entity.Page;
import cf.vsing.community.service.LikeService;
import cf.vsing.community.service.SearchService;
import cf.vsing.community.service.UserService;
import cf.vsing.community.util.StatusUtil;
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
public class SearchController {
    private final SearchService searchService;
    private final UserService userService;
    private final LikeService likeService;

    @Autowired
    SearchController(SearchService searchService, UserService userService, LikeService likeService){
        this.searchService = searchService;
        this.userService = userService;
        this.likeService = likeService;
    }

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model module) {
        Map<String, Object> result = searchService.search(keyword, null, page.getCurrent() - 1, page.getLimit());

        List<Map<String, Object>> data = new ArrayList<>();
        page.setPath("/search?keyword=" + keyword);
        page.setRows((Integer) result.get("total"));

        for (Article article : (List<Article>) result.get("articles")) {
            Map<String, Object> each = new HashMap<>();
            each.put("article", article);
            each.put("author", userService.findById(article.getUserId()));
            each.put("like", likeService.likeCount(StatusUtil.ENTITY_ARTICLE, article.getId()));
            data.add(each);
        }
        module.addAttribute("totalData", data);
        module.addAttribute("keyword", keyword);

        return "/site/search";
    }
}
