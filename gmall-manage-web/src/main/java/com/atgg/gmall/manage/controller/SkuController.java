package com.atgg.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.bean.PmsSkuInfo;
import com.atgg.gmall.service.SkuService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pstart
 * @create 2019-10-14 19:41
 */

@CrossOrigin
@RestController
public class SkuController {
    @Reference
    SkuService service;
    //拿一个表单页面所有不同类型值数据汇合时使用@requestBody
    // (例如数组中的数组pms_product_sale_attr,pms_product_sale_attr_value)
    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        service.saveSkuInfo(pmsSkuInfo);
        return "success";
    }
}
