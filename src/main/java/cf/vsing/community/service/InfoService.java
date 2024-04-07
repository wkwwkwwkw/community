package cf.vsing.community.service;

import cf.vsing.community.dao.InfoMapper;
import cf.vsing.community.entity.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InfoService {

    private final InfoMapper infoMapper;

    @Autowired
    InfoService(InfoMapper infoMapper1){
        this.infoMapper = infoMapper1;
    }

    public List<Info> getEvent(int userId, int offset, int limit) {
        return infoMapper.selectEvent(userId, offset, limit);
    }

    public List<Info> getDetail(int userId, String event, int offset, int limit) {
        return infoMapper.selectDetail(userId, event, offset, limit);
    }

    public int countEvent(int userId) {
        return infoMapper.countEvent(userId);
    }

    public int countDetail(int userId, String event) {
        return infoMapper.countDetail(userId, event);
    }

    public int countUnread(int userId, String event) {
        return infoMapper.countUnread(userId, event);
    }

    public int sendInfo(Info information) {
        return infoMapper.insertInfo(information);
    }

    public int readInfo(List<Integer> Ids) {
        return infoMapper.updateStatus(Ids, 1);
    }
}
