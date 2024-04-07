package cf.vsing.community.service;

import cf.vsing.community.entity.User;
import cf.vsing.community.util.RedisKeyUtil;
import cf.vsing.community.util.StatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService {

    private final UserService userService;
    private final ArticleService articleService;
    private final RedisTemplate redisTemplate;
    @Autowired
    FollowService(UserService userService, ArticleService articleService, RedisTemplate redisTemplate){
        this.userService = userService;
        this.articleService = articleService;
        this.redisTemplate = redisTemplate;
    }


    //  关注（用户、文章等）
    public boolean follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFollowee(userId, entityType);
                String followerKey = RedisKeyUtil.getFollower(entityType, entityId);
                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
        return true;
    }

    //  取消关注（用户、文章等）
    public boolean unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFollowee(userId, entityType);
                String followerKey = RedisKeyUtil.getFollower(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
        return true;
    }

    //  计算粉丝数量
    public long followerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollower(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //  计算某用户关注（的用户、文章等）的数量
    public long followeeCount(int entityType, int userId) {
        String followeeKey = RedisKeyUtil.getFollowee(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //  获取关注状态
    public boolean followStatus(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFollowee(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    //  获取某用户关注的详细列表
    public List<Map<String, Object>> findFollowees(int userId,int entityType, int offset, int limit) {
        List<Map<String, Object>> follower = new ArrayList<>();
        String followeeKey = RedisKeyUtil.getFollowee(userId, entityType);

        Set<Integer> fids = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (fids == null) {
            return follower;
        }
        for (int fid : fids) {
            Map<String, Object> map = new HashMap<>();
            if(entityType==StatusUtil.ENTITY_ARTICLE){
                map.put("data", articleService.findArticleById(fid));
            }else if (entityType==StatusUtil.ENTITY_USER) {
                map.put("data", userService.findById(fid));
            }
            map.put("date", new Date(redisTemplate.opsForZSet().score(followeeKey, fid).longValue()));
            follower.add(map);
        }
        return follower;
    }


    //  获取某用户粉丝的详细列表
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        List<Map<String, Object>> follower = new ArrayList<>();
        String followerKey = RedisKeyUtil.getFollower(StatusUtil.ENTITY_USER, userId);

        Set<Integer> fids = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (fids == null) {
            return follower;
        }
        for (int fid : fids) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findById(fid);
            map.put("user", user);
            map.put("date", new Date(redisTemplate.opsForZSet().score(followerKey, fid).longValue()));
            follower.add(map);
        }
        return follower;
    }

}
