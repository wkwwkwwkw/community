package cf.vsing.community.dao;

import cf.vsing.community.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//  OK!!!
@Mapper
public interface ArticleMapper {

    //  获取文章的最大ID
    int selectMaxId();

    //  按作者ID筛选文章，如果作者ID为0则筛选评分最高的文章（按页）
    List<Article> selectArticleByUserId(int userId, int offset, int limit);

    //  筛选指定ID的文章
    Article selectArticleById(int id);

    //  计算指定作者ID的文章总数，如果作者ID为0则计算所有文章总数
    int selectArticleRows(@Param("userId") int userId);

    //  插入新文章
    int insertArticle(Article article);

    //  更新文章的评论数
    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);
}
