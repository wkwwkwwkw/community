package cf.vsing.community.util;

public class RedisKeyUtil {
    public static final String SPLIT = ":";

    //赞
    public static final String PREFIX_LIKE = "like";
    public static final String PREFIX_LIKER = "liker";

    //关注
    public static final String PREFIX_FOLLOWEE = "followee";
    public static final String PREFIX_FOLLOWER = "follower";

    //验证码
    public static final String PREFIX_CAPTCHA = "captcha";

    //登录用户缓存池
    public static final String PREFIX_LOGIN = "login";

    //缓存:用户信息
    public static final String PREFIX_USER = "user";

    //缓存:各表最大ID
    public static final String PREFIX_TABLE = "table";

    public static String getLiker(int entityType, int entityId) {
        return PREFIX_LIKER + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getLike(int userId, int entityType) {
        return PREFIX_LIKE + SPLIT + userId + SPLIT + entityType;
    }

    //followee:userId:entityType->(entityId,score)
    //followee:userId:USER->(userId,score)
    //followee:userId:ARTICLE->(articleId,score)
    //followee:userId:COMMENT->(commentId,score)

    //follower:entityType:userId->(entityId,score)


    //follower:entityType:entityId->(userId,score)

    //follower:ARTICLE:articleId->(userId,score)
    //follower:USER:userId->(userId,score)
    //follower:COMMENT:commentId->(userId,score)

    public static String getFollower(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getFollowee(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    public static String getCaptcha(String sessionId) {
        return PREFIX_CAPTCHA + SPLIT + sessionId;
    }

    public static String getLogin(Integer userId) {
        return PREFIX_LOGIN + SPLIT +(userId==null?"":userId);
    }

    public static String getUser(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    public static String getTable(String name) {
        return PREFIX_TABLE + SPLIT + name;
    }


}
