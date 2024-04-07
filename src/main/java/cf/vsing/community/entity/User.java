package cf.vsing.community.entity;

import cf.vsing.community.util.StatusUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static cf.vsing.community.util.StatusUtil.Authority.*;

//  OK!!!
@JsonIgnoreProperties({"enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "authorities", "username"})
public class User implements UserDetails {
    private int id;
    private String password;
    private String salt;
    private int authority;
    private int status;
    private String name;
    private String header;
    private String detail;
    private String email;
    private String phone;
    private Date createTime;
    private String activationCode;


    //  SpringSecurity 的 UserDetail 接口函数，用于登录验证。

    //  用户名，SpringSecurity 的默认验证方式使用“用户名”和“密码”进行验证
    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    //  账户是否过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //  账户是否锁定
    @Override
    public boolean isAccountNonLocked() {
        return status == StatusUtil.Status.LOCKED.ordinal();
    }

    //  用户凭证（密码）是否过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //  账户是否可用
    @Override
    public boolean isEnabled() {
        return status == StatusUtil.Status.NORMAL.ordinal();
    }

    //  获取用户权限组
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        StatusUtil.Authority authority = switch (this.authority) {
            case 1 -> authority = RESERVED;
            case 2 -> authority = ADMIN;
            case 3 -> authority = SYSTEM;
            default -> authority = USER;
        };
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + authority.name()));
        return grantedAuthorities;
    }

    public int getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }


    public int getAuthority() {
        return authority;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getHeader() {
        return header;
    }

    public String getDetail() {
        return detail;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getActivationCode() {
        return activationCode;
    }


    public User setId(int id) {
        this.id = id;
        return this;
    }
    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public User setSalt(String salt) {
        this.salt = salt;
        return this;
    }

    public User setAuthority(int authority) {
        this.authority = authority;
        return this;
    }

    public User setStatus(int status) {
        this.status = status;
        return this;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public User setHeader(String header) {
        this.header = header;
        return this;
    }

    public User setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public User setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public User setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public User setActivationCode(String activationCode) {
        this.activationCode = activationCode;
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
                ", authority=" + authority +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", header='" + header + '\'' +
                ", detail='" + detail + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", createTime=" + createTime +
                ", activationCode='" + activationCode + '\'' +
                '}';
    }
}
