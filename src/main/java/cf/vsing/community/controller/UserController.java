package cf.vsing.community.controller;

import cf.vsing.community.entity.User;
import cf.vsing.community.service.FollowService;
import cf.vsing.community.service.LikeService;
import cf.vsing.community.service.UserService;
import cf.vsing.community.util.CommunityUtil;
import cf.vsing.community.util.FieldUtil;
import cf.vsing.community.util.HostHolderUtil;
import cf.vsing.community.util.StatusUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

@Controller
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final LikeService likeService;
    private final FollowService followService;
    private final HostHolderUtil hostHolder;
    @Value("${project.path.upload}")
    private String uploadPath;
    @Value("${project.path.domain}")
    private String domain;
    @Autowired
    UserController(UserService userService, LikeService likeService, FollowService followService, HostHolderUtil hostHolder){
        this.userService = userService;
        this.likeService = likeService;
        this.followService = followService;
        this.hostHolder = hostHolder;
    }


    @RequestMapping(value = "/user/setting", method = RequestMethod.GET)
    public String goSetting() {
        return "/site/setting";
    }

    @RequestMapping(value = "/user/space/{userId}", method = RequestMethod.GET)
    public String goSpace(@PathVariable("userId") int userId, Model model, HttpServletRequest request) {
        User user = userService.findById(userId);
        Integer selfId = hostHolder.getUserId(request);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        boolean isLogin = selfId != null;
        boolean isSelf = isLogin && selfId==userId;

        model.addAttribute("likeCount", likeService.likeCount(StatusUtil.ENTITY_USER, userId));
        model.addAttribute("followeeNum", followService.followeeCount(StatusUtil.ENTITY_USER, userId));
        model.addAttribute("followerNum", followService.followerCount(StatusUtil.ENTITY_USER, userId));
        model.addAttribute("followStatus", isLogin && followService.followStatus(selfId, StatusUtil.ENTITY_USER, userId));
        model.addAttribute("isSelf", isSelf);
        model.addAttribute("isLogin", isLogin);
        model.addAttribute("user", user);

        return "/site/space";
    }


    @RequestMapping(value = "/setting/header", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model, HttpServletRequest request) {

        //判空
        if (headerImage == null) {
            model.addAttribute("error", "请先选择头像文件！");
            return "redirect:/user/setting";
        }
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //判非法格式
        if (StringUtils.isBlank(suffix)||!suffix.matches(".(png|jpg|jpeg|webp|bmp|gif|ico|pcx|tif|raw|tga)")) {
            model.addAttribute("error", "请选择正确格式的图片！");
            return "redirect:/user/setting";
        }

        String localFileName = CommunityUtil.md5(headerImage) + suffix;
        File dest = new File(uploadPath + "/" + localFileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("用户上传头像文件保存出错: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        String headerUrl = domain + "/api/file/" + localFileName;
        userService.updateField(hostHolder.getUserId(request), FieldUtil.User.header, headerUrl);
        return "redirect:/";
    }

    @RequestMapping(path = "/setting/password", method = RequestMethod.POST)
    public String resetPassword(HttpServletRequest request, HttpServletResponse response, String oldPassword, String newPassword, Model model) {
        User self = hostHolder.getUser(request);
        if (self == null || StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPasswordMsg", "请输入原密码！");
            model.addAttribute("newPassword", newPassword);
            return "/site/setting";
        }
        if (StringUtils.isBlank(newPassword)) {
            model.addAttribute("newPasswordMsg", "请输入新密码！");
            model.addAttribute("oldPassword", oldPassword);
            return "/site/setting";
        }
        if (Objects.equals(CommunityUtil.md5(oldPassword + self.getSalt()), self.getPassword())) {
            userService.updateField(self.getId(), FieldUtil.User.password, CommunityUtil.md5(newPassword + self.getSalt()));
            return "redirect:/logout";
        }
        model.addAttribute("oldPasswordMsg", "原密码错误！");
        model.addAttribute("oldPassword", oldPassword);
        model.addAttribute("newPassword", newPassword);
        return "/site/setting";
    }


    @RequestMapping(value = "/api/file/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        String filePath = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (!StringUtils.isBlank(suffix)) {
            response.setContentType("image/" + suffix);
            try (
                    FileInputStream fs = new FileInputStream(filePath);
                    OutputStream os = response.getOutputStream()
            ) {

                byte[] buffer = new byte[1024];
                int b;
                while ((b = fs.read(buffer)) != -1) {
                    os.write(buffer, 0, b);
                }
            } catch (IOException e) {
                log.error("文件读取错误：" + e.getMessage());
                throw new RuntimeException(e);
            }
        }

    }
}
