package cf.vsing.community.util;


import com.alibaba.fastjson2.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommunityUtil {

    public static final String BASE_KEY = "akieof";
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    public static String generateJWT(int id,int auth, long expireMillis) {
        expireMillis=new Date().getTime()+expireMillis;
        return JWT.create()
                .withClaim("i",id)
                .withClaim("a",auth)
                .withClaim("e", expireMillis)
                .sign(Algorithm.HMAC512(BASE_KEY));
    }

    public static boolean isJWTExpired(DecodedJWT jwt){
        return System.currentTimeMillis() < jwt.getClaim("e").asLong();
    }
    public static DecodedJWT verifyJWT(String token) {
        try {
            return JWT.require(Algorithm.HMAC512(BASE_KEY)).build().verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }
    public static String md5(String key) {
        return StringUtils.isBlank(key) ? null : DigestUtils.md5DigestAsHex(key.getBytes());
    }
    public static String md5(MultipartFile file){
        try {
            return file.isEmpty()?null:DigestUtils.md5DigestAsHex(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getJSON(int code, String msg, Map<String, Object> data) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (data != null) {
            json.put("data", data);
        }
        return json.toJSONString();
    }
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return request.getHeader("x-requested-with").equals("XMLHttpRequest");
    }
}
