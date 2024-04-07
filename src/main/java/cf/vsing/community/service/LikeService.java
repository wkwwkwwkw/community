package cf.vsing.community.service;

import cf.vsing.community.util.RedisKeyUtil;
import cf.vsing.community.util.StatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    private final RedisTemplate redisTemplate;
    @Autowired
    LikeService(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    //点赞
    public boolean like(int authorId, int userId, int entityType, int entityId) {

        //用户的点赞列表
        //eg:   like:userId:entityType  [entityId,entityId,...]
        String likeKey = RedisKeyUtil.getLike(userId, entityType);
        //帖子、评论的获赞列表
        //eg:   liker:entityType:entityId   [userId,userId,...]
        String likerKey = RedisKeyUtil.getLiker(entityType, entityId);
        //帖子、评论作者获赞数目
        //eg:   liker:0:authorId   num
        String totalLike = RedisKeyUtil.getLiker(StatusUtil.ENTITY_USER, authorId);
        //点赞状态，true表示已点赞
        boolean status = redisTemplate.opsForSet().isMember(likerKey, userId);
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                operations.multi();
                if (status) {
                    operations.opsForSet().remove(likeKey, entityId);
                    operations.opsForSet().remove(likerKey, userId);
                    operations.opsForValue().decrement(totalLike);
                } else {
                    operations.opsForSet().add(likeKey, entityId);
                    operations.opsForSet().add(likerKey, userId);
                    operations.opsForValue().increment(totalLike);
                }
                return operations.exec();
            }
        });
        return true;
    }

    //检查用户点赞状态
    public boolean status(int userId, int entityType, int entityId) {
        String likerKey = RedisKeyUtil.getLiker(entityType, entityId);
        return redisTemplate.opsForSet().isMember(likerKey, userId);
    }

    //查询点赞数
    public long likeCount(int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLiker(entityType, entityId);
        if (entityType == StatusUtil.ENTITY_USER) {
            Integer count = (Integer) redisTemplate.opsForValue().get(likeKey);
            return count == null ? 0 : count;
        }
        return redisTemplate.opsForSet().size(likeKey);
    }
}
