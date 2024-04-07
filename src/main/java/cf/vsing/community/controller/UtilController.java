package cf.vsing.community.controller;

import cf.vsing.community.util.CommunityUtil;
import cf.vsing.community.util.CookieUtil;
import cf.vsing.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@Controller
public class UtilController {
    private static final Logger log = LoggerFactory.getLogger(UtilController.class);
    private final Producer kaptcha;
    private final RedisTemplate<String,Object> redisTemplate;
    @Autowired
    UtilController(Producer kaptcha, RedisTemplate<String,Object> redisTemplate){
        this.kaptcha = kaptcha;
        this.redisTemplate = redisTemplate;
    }


    @RequestMapping(path = "/captcha", method = RequestMethod.GET)
    public void GetCaptcha(HttpServletRequest request, HttpServletResponse response) {
        String captchaId= CookieUtil.getCookie(request,"captchaId");
        if(captchaId!=null){
            redisTemplate.delete(RedisKeyUtil.getCaptcha(captchaId));
        }

        String code = kaptcha.createText();
        String id= CommunityUtil.generateUUID();

        Cookie cookie = new Cookie("captchaId", id);
        cookie.setMaxAge(300);
        cookie.setPath("/login");
        response.addCookie(cookie);

        BufferedImage image=kaptcha.createImage(code);
        response.setContentType("image/png");

        redisTemplate.opsForValue().set(RedisKeyUtil.getCaptcha(id), code, 300, TimeUnit.SECONDS);
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            log.error("验证码生产失败" + e.getMessage());
        }
    }

}
