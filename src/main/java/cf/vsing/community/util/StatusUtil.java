package cf.vsing.community.util;

public interface StatusUtil {

    //账户激活请求返回状态码
    int ACTIVATION_NULL = 0;
    int ACTIVATION_OK = 1;
    int ACTIVATION_REPEAT = 2;
    int ACTIVATION_WRONG = 3;

    //用户登录信息过期时间
    int DEFAULT_EXPIRED_SECONDS = 3600 * 24;
    int LONG_EXPIRED_SECONDS = 3600 * 24 * 15;

    //实体类型
    int ENTITY_USER = 0;
    int ENTITY_ARTICLE = 1;
    int ENTITY_COMMENT = 2;

    //文章默认显示回复条数
    int REPLAY_SHOW_NUMBER = 3;


    //我也忘了是啥
    int FIND_TYPE_FOLLOWEE = 1;
    int FIND_TYPE_FOLLOWER = 2;

    //系统用户ID
    int SYSTEM_INFO = 0;

    //消息队列TOPIC
    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";
    String TOPIC_ARTICLE = "article";


    //用户权限
    enum Authority {
        USER(0),
        RESERVED(1),
        ADMIN(2),
        SYSTEM(3);
        final int value;

        Authority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    //用户状态
    enum Status {
        UNACTIVATED,
        NORMAL,
        DELETED,
        LOCKED,
        UNKNOWN;

        public static Status of(int value) {
            for (Status status : values()) {
                if (status.ordinal() == value) {
                    return status;
                }
            }
            return UNKNOWN;
        }
    }

}
