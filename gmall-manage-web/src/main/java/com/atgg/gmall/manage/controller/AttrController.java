package com.atgg.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.bean.PmsBaseAttrInfo;
import com.atgg.gmall.service.AttrService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-11 21:52
 */
@RestController
@CrossOrigin
public class AttrController {
    @Reference
    AttrService attrService;

    @RequestMapping("attrInfoList")
    public List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id){
        return attrService.getAttrInfoList(catalog3Id);
    }


    //保存和修改写入一个方法的实现类里
    @RequestMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){
        attrService.saveAttrInfo(pmsBaseAttrInfo);
        return "success";
    }

    //点击修改后回显的内容
    @RequestMapping("getAttrValueList")
    public PmsBaseAttrInfo getAttrValueList(String attrId){
        PmsBaseAttrInfo params = attrService.getAttrValueList(attrId);
        return params;
    }


}
