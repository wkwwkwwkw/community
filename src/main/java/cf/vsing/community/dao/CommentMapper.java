package cf.vsing.community.dao;

import cf.vsing.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
//  OK!!!
@Mapper
public interface CommentMapper {

    int selectMaxId();
    Comment selectById(int id);
    List<Comment> selectByEntityId(int entityType, int entityId, int offset, int limit);

    int countByEntityId(int entityType, int entityId);

    int insertComment(Comment comment);
}
