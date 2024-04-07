package cf.vsing.community.service;

import cf.vsing.community.dao.ArticleMapper;
import cf.vsing.community.entity.Article;
import cf.vsing.community.util.SensitiveWorldUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

//todo:findArticleById
@Service
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final SearchService searchService;
    private final SensitiveWorldUtil sensitiveFilter;

    //  OK!!!
    @Autowired
    ArticleService(ArticleMapper articleMapper, SearchService searchService, SensitiveWorldUtil sensitiveFilter){
        this.articleMapper = articleMapper;
        this.searchService = searchService;
        this.sensitiveFilter = sensitiveFilter;
    }

    //  OK!!!
    //  计算（某用户）文章总数，如果传入用户ID为0则计算所有文章数目
    public int countArticle(int userId) {
        return articleMapper.selectArticleRows(userId);
    }

    //  OK!!!
    //  获取（某用户）文章列表，如果传入用户ID为0则筛选最高分的文章（按页）
    public List<Article> findArticleByUserId(int userId, int offset, int limit) {
        return articleMapper.selectArticleByUserId(userId, offset, limit);
    }

    //  OK!!!
    //  按ID查找单篇文章(不包括已封禁和删除内容)
    public Article findArticleById(int id) {
        //2-删除，3-封禁
        Article article = articleMapper.selectArticleById(id);
        if(article!=null &&(article.getStatus()==0||article.getStatus()==1)){
            return article;
        }
        return null;
    }

    //  OK!!!
    //  添加文章
    public boolean addArticle(Article article) {
        article.setTitle(HtmlUtils.htmlEscape(article.getTitle()));
        article.setContent(HtmlUtils.htmlEscape(article.getContent()));
        article.setTitle(sensitiveFilter.filter(article.getTitle()));
        article.setContent(sensitiveFilter.filter(article.getContent()));
        return articleMapper.insertArticle(article) == 1;
    }

    //  OK!!!
    //  更新文章评论数量
    public boolean updateCommentCount(int id, int commentCount) {
        return articleMapper.updateCommentCount(id, commentCount)==1;
    }

    public boolean updateType(int id, int type) {
        return articleMapper.updateType(id, type)==1;
    }

    public boolean updateStatus(int id, int status) {
        return articleMapper.updateStatus(id, status)==1;
    }

    //  OK!!!
    //  获得最大ID（String）
    public String getMaxId() {
        return String.format("%09d", articleMapper.selectMaxId());
    }
}
