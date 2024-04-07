package cf.vsing.community.dao;

import cf.vsing.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    List<Message> selectConversation(int userId, int offset, int limit);

    List<Message> selectDetail(String conversationId, int offset, int limit);

    int selectMaxId();

    int updateStatus(List<Integer> id, int status);

    int insertMessage(Message message);

    int countConversation(int userId);

    int countDetail(String conversationId);

    int countUnread(int userId, String conversationId);

}
