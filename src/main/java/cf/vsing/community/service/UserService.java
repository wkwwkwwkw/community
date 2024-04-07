package cf.vsing.community.service;

import cf.vsing.community.dao.UserMapper;
import cf.vsing.community.entity.User;
import cf.vsing.community.service.redisCache.UserCache;
import cf.vsing.community.util.CommunityUtil;
import cf.vsing.community.util.FieldUtil;
import cf.vsing.community.util.MailUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.vsing.community.util.StatusUtil.Authority;
import static cf.vsing.community.util.StatusUtil.Status;

// todo：findById
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserCache cache;
    private final MailUtil mailUtil;
    private final TemplateEngine templateEngine;

    @Autowired
    UserService(UserMapper userMapper, UserCache cache, MailUtil mailUtil, TemplateEngine templateEngine) {
        this.userMapper = userMapper;
        this.cache = cache;
        this.mailUtil = mailUtil;
        this.templateEngine = templateEngine;
    }

    //  获取用户最大ID（String）
    public String getMaxId() {
        return String.format("%09d", userMapper.selectMaxId());
    }

    //  用户注册
    public Map<String, Object> register(@NonNull User user,String host) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(user.getName())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //重复注册判断
        if (userMapper.selectByName(user.getName()) != null) {
            map.put("usernameMsg", "用户已存在！");
            return map;
        }
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg", "邮箱已被注册！");
            return map;
        }

        //用户信息初始化
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setAuthority(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeader("http://images.nowcoder.com/head/1t.png");
        user.setCreateTime(new Date());


        userMapper.insertUser(user);
        String activationUrl = host + "/activation/" + user.getId() + "/" + user.getActivationCode();

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("url", activationUrl);
        String emailText = templateEngine.process("/mail/activation", context);
        mailUtil.send(user.getEmail(), "Vsing邮箱验证", emailText);

        return map;
    }

    //  新用户激活
    public Map<String, Object> activation(int id, String activationCode) {
        Map<String, Object> map = new HashMap<>();
        User user = userMapper.selectById(id);
        if (user == null) {
            map.put("msg", "操作无效，用户未注册！");
        } else if (user.getStatus() == 1) {
            map.put("msg", "操作无效，用户已激活！");
        } else if (user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(id, 1);
            cache.flushCache(id);
            map.put("ok", true);
            map.put("msg", "激活成功，您的账号以经可以正常使用！");
        } else {
            map.put("msg", "激活失败，激活码错误！");
        }
        return map;
    }

    //按域查找
    public User find(FieldUtil.User field,String value) {
        return switch (field){
            case email -> userMapper.selectByEmail(value);
            case name -> userMapper.selectByName(value);
            default -> null;
        };
    }
    //  按UID查找用户（使用缓存）
    public User findById(int id) {
        return cache.getCache(id);
    }

    //  按用户组查找用户
    public List<User> findByAuthority(Authority authority) {
        return userMapper.selectByAuthority(authority.getValue());
    }

    //  按用户状态查找用户
    public List<User> findByStatus(Status status) {
        return userMapper.selectByAuthority(status.ordinal());
    }

    //  修改用户组
    public boolean updateAuthority(int id, Authority authority) {
        cache.flushCache(id);
        return userMapper.updateAuthority(id, authority.getValue()) == 1;
    }

    //  修改账户状态
    public boolean updateField(int id, FieldUtil.User field, String value){
        boolean success=false;
        cache.flushCache(id);
        switch (field){
            case header:
                success=userMapper.updateHeader(id,value);
                cache.flushCache(id);
                break;
            case password:
                cache.flushCache(id);
                success=userMapper.updatePassword(id,value);
                break;
        }
        return success;
    }
}
