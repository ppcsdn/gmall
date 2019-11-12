package com.atgg.gmall.passport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TestSessionController {

    Map<String,HttpSession> map = new HashMap<>();


    @RequestMapping("someOthers")
    @ResponseBody
    public String someOthers(HttpSession session){

        String userId = "1";

        String user = (String)session.getAttribute(userId);

        if(user!=null){
            return "访问成功";
        }else{
            return "访问失败";
        }
    }

    @RequestMapping("oneLogin")
    @ResponseBody
    public String oneLogin(HttpSession session){

        String userId  = "1";

        session.setAttribute(userId,"user");

        HttpSession httpSession = map.get(userId);
        if(httpSession!=null){
            httpSession.invalidate();
        }
        map.put(userId,session);

        return session.getId();
    }

}
