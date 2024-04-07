package cf.vsing.community.service;

import cf.vsing.community.dao.MessageMapper;
import cf.vsing.community.entity.Message;
import cf.vsing.community.util.SensitiveWorldUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    private final MessageMapper messageMapper;
    private final SensitiveWorldUtil sensitiveWorldFilter;
    @Autowired
    MessageService(MessageMapper messageMapper, SensitiveWorldUtil sensitiveWorldFilter){
        this.messageMapper = messageMapper;
        this.sensitiveWorldFilter = sensitiveWorldFilter;
    }


    public List<Message> getConversation(int userId, int offset, int limit) {
        return messageMapper.selectConversation(userId, offset, limit);
    }

    public List<Message> getDetail(String conversationId, int offset, int limit) {
        return messageMapper.selectDetail(conversationId, offset, limit);
    }

    public int countConversation(int userId) {
        return messageMapper.countConversation(userId);
    }

    public int countDetail(String conversationId) {
        return messageMapper.countDetail(conversationId);
    }

    public int countUnread(int userId, String conversationId) {
        return messageMapper.countUnread(userId, conversationId);
    }

    public int sendMsg(Message message) {
        message.setContent(sensitiveWorldFilter.filter(HtmlUtils.htmlEscape(message.getContent())));
        return messageMapper.insertMessage(message);
    }

    public int readMsg(List<Integer> Ids) {
        return messageMapper.updateStatus(Ids, 1);
    }
}
