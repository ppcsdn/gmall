package com.atgg.gmall.user.controller;

import com.atgg.gmall.bean.UmsMember;
import com.atgg.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.dubbo.config.annotation.Reference;

import java.util.List;

@Controller
public class UserController {

    @Reference
    UserService userService;

    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "index";
    }


    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMembers = userService.getAllUser();

        return umsMembers;
    }


    @RequestMapping("getUserById")
    @ResponseBody
    public UmsMember geUserById(String memberId){

        UmsMember umsMember = userService.getUserById(memberId);
        return umsMember;
    }


}
