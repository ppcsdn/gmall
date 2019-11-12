package com.atgg.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.bean.UmsMember;
import com.atgg.gmall.cart.util.HttpclientUtil;
import com.atgg.gmall.cart.util.JwtUtil;
import com.atgg.gmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller//认证中心
public class PassportController {
    @Reference
    UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(HttpServletRequest request, String code) {
        // 交换access_token
        Map<String, String> mapCode = new HashMap<>();
        mapCode.put("client_id", "2333353958");
        mapCode.put("client_secret", "ef392a889744bd46e97c35148fa71938");
        mapCode.put("grant_type", "authorization_code");
        mapCode.put("redirect_uri", "http://passport.gmall.com:8085/vlogin");
        mapCode.put("code", code);
        String access_token_return = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token", mapCode);
        Map<String, Object> mapResult = new HashMap<>();
        mapResult = JSON.parseObject(access_token_return, mapResult.getClass());
        String access_token = (String) mapResult.get("access_token");
        String uid = (String) mapResult.get("uid");

        // 用access_token交换用户信息
        String user_reqire = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;//2.00O7zagC0d12U_8ecdf71426uHIMEE&uid=2461687694
        String user_return = HttpclientUtil.doGet(user_reqire);
        mapResult = JSON.parseObject(user_return, mapResult.getClass());
        UmsMember umsMember = new UmsMember();
        // 保存第三方用户信息
        umsMember.setId("1");
        umsMember.setNickname("tom");
        // 登录成功生成token
        String token = "";
        // 服务器密钥
        String serverKey = "gmallServer";
        // 浏览器盐值
        String ip = "";
        ip = request.getHeader("x-forward-for");
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();
        }
        Map<String, Object> mapToken = new HashMap<>();
        mapToken.put("userId", umsMember.getId());
        mapToken.put("nickName", umsMember.getNickname());
        token = JwtUtil.encode(serverKey, mapToken, ip);

        return "redirect:http://search.gmall.com:8083/index?newToken=" + token;
    }

    @RequestMapping("verify")//校验token
    @ResponseBody
    public String verify(String token,HttpServletRequest request,String requestIp) {

        /*去中心化操作jwt 根据指定的服务器密钥的值来确定用户的登录状态,也就是说decode能够解密成功即代表用户为登录状态*/
        String ip = request.getHeader("x-forward-for");//使用getRemoteAddr()方法获取真实的浏览器url
        if (StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();
        }
        String remoteAddr = request.getRemoteAddr();//服务器密匙
        Map<String, Object> gmallServer = JwtUtil.decode(token, "gmallServer", requestIp);
        /*去中心化操作*/


        Map<String, String> map = new HashMap<>();//用户信息
        //验证token是否正确,验证成功返回给拦截器信息后cookie覆盖保存
        UmsMember umsMember = userService.veryfyToken(token);
        if (umsMember != null) {//只要不为null即代表token成功保存在了缓存中取出即可
            map.put("userId", umsMember.getId());
            map.put("nickName", umsMember.getNickname());
            map.put("success", "success");
        }else {
            map.put("success" , "fail");//作用显示判断
        }
        String resultInfo = JSON.toJSONString(map);
        return resultInfo;
    }


    @RequestMapping("login")//返回token和用户信息
    @ResponseBody
    public String login(HttpServletRequest request, String loginName, String passwd, ModelMap modelMap) {
        String token = "";
        //调用userService,核对用户名密码
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(loginName);
        umsMember.setPassword(passwd);
        umsMember = userService.login(umsMember);//用户具体信息包括收获地址
        if (umsMember != null) {
            //生产token
            String serverKey = "gmallServer";//服务器密匙
//            String ip = "127.0.0.1";//浏览器盐值
            String ip = request.getHeader("x-forward-for");//使用getRemoteAddr()方法获取真实的浏览器url
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
            }

            Map<String, Object> map = new HashMap<>();//用户信息
            map.put("userId", umsMember.getId());
            map.put("nickName", umsMember.getNickname());
            //生产jwttoken
            token = JwtUtil.encode(serverKey, map, ip);
            if (StringUtils.isNotBlank(token)) {
                //验证通过后,将token写入redis缓存
                userService.setUserTokenToCache(token, umsMember.getId());
            }
        } else {
            token = "fail";//作用显示判断
        }
        //返回token
        return token;
    }

    @RequestMapping("index")//即用户验证失败要跳转的页面index首页
    public String index(String ReturnUrl, ModelMap modelMap) {
        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";
    }
}
