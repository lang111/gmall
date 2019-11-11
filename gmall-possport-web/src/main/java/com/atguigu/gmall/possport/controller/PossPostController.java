package com.atguigu.gmall.possport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.possport.util.JwtUtil;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PossPostController {

    @Autowired
    private JwtUtil jwtUtil;
    @Value("${token.key}")
    private String key;
    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");

        request.setAttribute("originUrl", originUrl);

        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){

        UserInfo info = this.userInfoService.login(userInfo);

        if(info!=null){
            String salt = request.getHeader("X-forwarded-for");
            HashMap<String, Object> map = new HashMap<>();
            map.put("nickName", info.getNickName());
            map.put("userId", info.getId());
            String token = this.jwtUtil.encode(key, map, salt);

            System.out.println("token"+token);
            return token;
        }else {

            return "fail";
        }
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");

        Map<String, Object> map = this.jwtUtil.decode(token, key, salt);
        if(map!=null){

            String userId = (String) map.get("userId");

            UserInfo info = this.userInfoService.verify(userId);

            if(info!=null){

                return "success";
            }
        }
            return "fail";
    }
}
