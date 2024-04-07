package cf.vsing.community.service;

import cf.vsing.community.dao.CommentMapper;
import cf.vsing.community.entity.Comment;
import cf.vsing.community.util.SensitiveWorldUtil;
import cf.vsing.community.util.StatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

//todo:getById
@Service
public class CommentService implements StatusUtil {
    private final CommentMapper commentMapper;
    private final ArticleService articleService;
    private final SensitiveWorldUtil sensitiveWorldFilter;
    @Autowired
    CommentService(CommentMapper commentMapper, ArticleService articleService, SensitiveWorldUtil sensitiveWorldFilter){
        this.commentMapper = commentMapper;
        this.articleService = articleService;
        this.sensitiveWorldFilter = sensitiveWorldFilter;
    }

    //获取单条评论
    public Comment getById(int id) {
        return commentMapper.selectById(id);
    }

    //  获取评论列表
    public List<Comment> getByEntityId(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectByEntityId(entityType, entityId, offset, limit);
    }

    //  计算评论数量
    public int countByEntityId(int entityType, int entityId) {
        return commentMapper.countByEntityId(entityType, entityId);
    }

    //  添加评论
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public boolean addComment(Comment comment) {
        comment.setContent(sensitiveWorldFilter.filter(HtmlUtils.htmlEscape(comment.getContent())));
        int rows = commentMapper.insertComment(comment);
        if (rows==1&&comment.getEntityType() == StatusUtil.ENTITY_ARTICLE) {
            int count = commentMapper.countByEntityId(comment.getEntityType(), comment.getEntityId());
            return articleService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows==1;
    }
}
