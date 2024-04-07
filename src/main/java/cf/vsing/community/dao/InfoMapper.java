package cf.vsing.community.dao;

import cf.vsing.community.entity.Info;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InfoMapper {
    List<Info> selectEvent(int userId, int offset, int limit);

    List<Info> selectDetail(int userId, String event, int offset, int limit);

    int selectMaxId();

    int updateStatus(List<Integer> id, int status);

    int insertInfo(Info information);

    int countEvent(int userId);

    int countDetail(int userId, String event);

    int countUnread(int userId, String event);
}
