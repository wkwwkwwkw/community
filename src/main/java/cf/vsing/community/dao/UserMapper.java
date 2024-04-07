package cf.vsing.community.dao;

import cf.vsing.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

//  OK!!!
@Mapper
public interface UserMapper {
    User selectField(String field, String value);
    User selectByName(String name);
    User selectByEmail(String email);
    User selectById(int id);

    List<User> selectByAuthority(int authority);
    List<User> selectByStatus(int status);

    int selectMaxId();

    int insertUser(User user);

    int updateAuthority(int id, int authority);
    int updateStatus(int id, int status);

    boolean updateHeader(int id, String value);

    boolean updatePassword(int id, String value);
}
