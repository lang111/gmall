package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;

import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getParameter("newToken");
        CookieUtil cookieUtil = new CookieUtil();
        if(token!=null){
            cookieUtil.setCookie(request,response,"token",token, WebConst.COOKIE_MAXAGE,false);
        }
        if(token==null){
            token= cookieUtil.getCookieValue(request, "token", false);
        }
        if(token!=null){
            Map tokenMap = getUserMapByToken(token);
            String nickName = (String) tokenMap.get("nickName");
            request.setAttribute("nickName", nickName);
            
        }
        HandlerMethod handlerMethod =(HandlerMethod) handler;
        LoginRequire loginRequire = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(loginRequire!=null){
            String salt = request.getHeader("X-forwarded-for");
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if("success".equals(result)){
               Map tokenMap = getUserMapByToken(token);
                String userId = (String) tokenMap.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else{
                if(loginRequire.autoRedirect()) {
                    String requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    //http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F35.html
                    String rediert = WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL;
                    response.sendRedirect(rediert);
                    return false;

                }

            }
        }

        return true;
    }

    private Map getUserMapByToken(String token) {
        String tokenMapStr = StringUtils.substringBetween(token, ".");
        byte[] bytes = new Base64UrlCodec().decode(tokenMapStr);
        String tokenStr = null;
        try {
            tokenStr = new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("tokenStr:"+tokenStr);
        Map map = JSON.parseObject(tokenStr, Map.class);
        return map;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
