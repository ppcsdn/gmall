package com.atgg.gmall.cart.interceptors;

import com.alibaba.fastjson.JSON;

import com.atgg.gmall.cart.annotations.LoginRequired;
import com.atgg.gmall.cart.util.CookieUtil;
import com.atgg.gmall.cart.util.HttpclientUtil;
import com.atgg.gmall.cart.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod hm = (HandlerMethod) handler;//自定义注解判断是否需要拦截该请求
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);//代理对象
        if (methodAnnotation == null) {//对写@LoginRequired注解的方法的请求拦截
            return true;
        }
        String token = "";//已经登录过从浏览器客户端cookie中拿到登录状态的唯一标识token
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }//第一次登录从地址栏URL获取newToken
        String newToken = request.getParameter("newToken");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        if (StringUtils.isNotBlank(token)) {//登录过,使用基于soap的http规范的httpclient远程RPC访问认证中心
            String ip = request.getHeader("x-forward-for");//使用getRemoteAddr()方法获取真实的浏览器url
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
            }
            String result = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token+"&requestIp="+ip);
            Map<String, String> resultMap = new HashMap<>();//用户信息
            resultMap = JSON.parseObject(result, resultMap.getClass());

            /*去中心化操作jwt*/
            String remoteAddr = request.getRemoteAddr();//服务器密匙
            Map<String, Object> gmallServer = JwtUtil.decode(token, "gmallServer", ip);
            /*去中心化操作*/

            if (StringUtils.isNotBlank(result) && "success".equals(resultMap.get("success"))) {//认证中心的认证结果及用户信息
                //刷新redis服务及客户端cookie的过期时间,覆盖Cookie
                CookieUtil.setCookie(request, response, "oldToken", token, 30 * 60, true);
                String userId = resultMap.get("userId");
                String nickName = resultMap.get("nickName");
                request.setAttribute("userId", userId);
                request.setAttribute("nickName", nickName);
                return true;//
            } else {//token验证失败,前一秒还没有过期,后一秒过期了
                if (methodAnnotation.isNeedSuccess()==false){//控制写了false的全部放行
                    return true;
                }
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl=" + request.getRequestURL());
                return false;
            }
        } else {//cookie里oldToken过期或newToken不存在,未登录,重定向到登录页面并重新认证
            if (methodAnnotation.isNeedSuccess()==false){//控制写了false的全部放行
                return true;
            }
            response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl=" + request.getRequestURL());
            return false;
        }
    }
}