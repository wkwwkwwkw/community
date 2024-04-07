package cf.vsing.community.util;

import cf.vsing.community.entity.User;
import cf.vsing.community.service.UserService;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static cf.vsing.community.util.RedisKeyUtil.getLogin;

@Component
public class HostHolderUtil {
    private static RedisTemplate<String,Object> redisTemplate;
    private static final Map<Integer,Long> loginStatus=new HashMap<>();
    private static boolean exist=false;

    static {

    }

    @Autowired
    public HostHolderUtil(RedisTemplate<String, Object> redisTemplate) {
        if(!exist){
            HostHolderUtil.redisTemplate = redisTemplate;
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                redisTemplate.keys(getLogin(null)+"*").forEach(k->{
                    loginStatus.put(Integer.parseInt(k.substring(getLogin(null).length())),connection.ttl(k.getBytes(),TimeUnit.MILLISECONDS)+new Date().getTime());
                });
                return null;
            });
            exist=true;
        }
    }





    public boolean checkLogin(int userId){
        return loginStatus.containsKey(userId) && loginStatus.get(userId) > new Date().getTime();
    }

    public Long getExpire(int userId){
        return loginStatus.get(userId);
    }
    public User getUser(HttpServletRequest request) {
        String jwt=CookieUtil.getCookie(request,"jwt");
        return jwt==null?null: getUser(jwt);
    }
    public User getUser(String jwt) {
        DecodedJWT claims=CommunityUtil.verifyJWT(jwt);
        return claims==null?null:getUser(claims.getClaim("i").asInt());
    }
    public User getUser(int userId) {
        return (User) redisTemplate.opsForValue().get(getLogin(userId));
    }

    public Integer getUserId(HttpServletRequest request) {
        String jwt=CookieUtil.getCookie(request,"jwt");
        DecodedJWT claims=CommunityUtil.verifyJWT(jwt);
        return claims==null?null:claims.getClaim("i").asInt();
    }
    public void setUser(User user, long timeout) {
        loginStatus.put(user.getId(),timeout + new Date().getTime());
        redisTemplate.opsForValue().set(getLogin(user.getId()),user,timeout,TimeUnit.MILLISECONDS);
    }

    public void update(User user){
        redisTemplate.opsForValue().setIfPresent(getLogin(user.getId()),user);
    }

    public void delete(int userId){
        loginStatus.remove(userId);
        redisTemplate.delete(getLogin(userId));
    }
    public void delete(String jwt){
        DecodedJWT temp=CommunityUtil.verifyJWT(jwt);
        if(temp!=null) {
            int userId =temp.getClaim("i").asInt();
            loginStatus.remove(userId);
            redisTemplate.delete(getLogin(userId));
        }
    }
    public void delete(HttpServletRequest request){
        String jwt=CookieUtil.getCookie(request,"jwt");
        delete(jwt);
    }

    public void clear() {
        loginStatus.clear();
        Set<String> keys = redisTemplate.keys(getLogin(null)+"*");
        if(keys!=null&&!keys.isEmpty()){
            redisTemplate.delete(keys);
        }
    }
}
