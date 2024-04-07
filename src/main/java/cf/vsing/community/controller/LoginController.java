package cf.vsing.community.controller;

import cf.vsing.community.entity.User;
import cf.vsing.community.service.UserService;
import cf.vsing.community.util.StatusUtil;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class LoginController implements StatusUtil {
    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }




    @RequestMapping(path = "/login/register", method = RequestMethod.GET)
    public String goRegister() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String goLogin(Model model, String error) {
        if(error!=null&&!StringUtils.isBlank(error)){
            JSONObject.parse(error).forEach(model::addAttribute);
        }
        return "/site/login";
    }


    @RequestMapping(path = "/login/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        if (user != null) {
            Map<String, Object> msg = userService.register(user,"");
            if (msg == null || msg.isEmpty()) {
                model.addAttribute("msg", "注册成功，请查看邮箱激活账号");
                model.addAttribute("target", "/login");
                return "/site/operate-result";
            } else {
                model.addAttribute("usernameMsg", msg.get("usernameMsg"));
                model.addAttribute("passwordMsg", msg.get("passwordMsg"));
                model.addAttribute("emailMsg", msg.get("emailMsg"));
                return "/site/register";
            }
        }
        return "/site/register";
    }



    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("token") String token) {
        //userService.logout(token);
        return "redirect:/";
    }


    @RequestMapping(path = "/activation/{userId}/{activationCode}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode) {
        Map<String,Object> info = userService.activation(userId, activationCode);
        model.addAttribute("msg", info.get("msg"));
        model.addAttribute("target", info.containsKey("ok")?"/login":"/");
        return "/site/operate-result";
    }

}
