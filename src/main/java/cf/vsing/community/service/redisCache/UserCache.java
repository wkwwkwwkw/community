package cf.vsing.community.service.redisCache;

import cf.vsing.community.dao.UserMapper;
import cf.vsing.community.entity.User;
import cf.vsing.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserCache implements Cache {
    private final RedisTemplate<String,Object> redisTemplate;
    private final UserMapper userMapper;

    @Autowired
    public UserCache(RedisTemplate<String, Object> redisTemplate, UserMapper userMapper) {
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
    }

    //  加载缓存
    public void loadCache(User user) {
        String key = RedisKeyUtil.getUser(user.getId());
        redisTemplate.opsForValue().setIfAbsent(key, user, TIMEOUT_USER, TimeUnit.SECONDS);
    }

    public User loadCache(int id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            redisTemplate.opsForValue().set(RedisKeyUtil.getUser(id), user, TIMEOUT_USER, TimeUnit.SECONDS);
        }
        return user;
    }

    //  使用缓存
    public User getCache(int id) {
        User user = (User) redisTemplate.opsForValue().get(RedisKeyUtil.getUser(id));
        return user != null ? user : loadCache(id);
    }

    //  刷新缓存
    public void flushCache(int id) {
        String key = RedisKeyUtil.getUser(id);
        redisTemplate.delete(key);
    }
}
