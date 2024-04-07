package cf.vsing.community.service.redisCache;

import cf.vsing.community.dao.*;
import cf.vsing.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MaxIdCache implements Cache {

    private final RedisTemplate<String,Integer> redisTemplate;

    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final InfoMapper infoMapper;
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    @Autowired
    public MaxIdCache(RedisTemplate redisTemplate, UserMapper userMapper, MessageMapper messageMapper, InfoMapper infoMapper, CommentMapper commentMapper, ArticleMapper articleMapper) {
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
        this.infoMapper = infoMapper;
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        loadCache("");
    }

    public String getCache(String name) {
        Object id=null;
        switch (name) {
            case "user", "message", "info", "comment", "article" -> {
                String key=RedisKeyUtil.getTable(name);
                if(redisTemplate.opsForValue().get(key)==null){
                    loadCache(name);
                }
                redisTemplate.opsForValue().increment(key);
                id = redisTemplate.opsForValue().get(key);
            }
        }
        return id==null?null:String.format("%09d", (int) id);
    }

    public void loadCache(String name) {
        switch (name) {
            case "user" -> redisTemplate.opsForValue().set(RedisKeyUtil.getTable("user"), userMapper.selectMaxId());
            case "message" -> redisTemplate.opsForValue().set(RedisKeyUtil.getTable("message"), messageMapper.selectMaxId());
            case "info" -> redisTemplate.opsForValue().set(RedisKeyUtil.getTable("info"), infoMapper.selectMaxId());
            case "comment" -> redisTemplate.opsForValue().set(RedisKeyUtil.getTable("comment"), commentMapper.selectMaxId());
            case "article" -> redisTemplate.opsForValue().set(RedisKeyUtil.getTable("article"), articleMapper.selectMaxId());
            default -> {
                redisTemplate.opsForValue().set(RedisKeyUtil.getTable("user"), userMapper.selectMaxId());
                redisTemplate.opsForValue().set(RedisKeyUtil.getTable("message"), messageMapper.selectMaxId());
                redisTemplate.opsForValue().set(RedisKeyUtil.getTable("info"), infoMapper.selectMaxId());
                redisTemplate.opsForValue().set(RedisKeyUtil.getTable("comment"), commentMapper.selectMaxId());
                redisTemplate.opsForValue().set(RedisKeyUtil.getTable("article"), articleMapper.selectMaxId());
            }
        }
    }

    public void flashCache(String name) {
        String key = RedisKeyUtil.getTable(name);
        redisTemplate.delete(key);
        loadCache(name);
    }
}
