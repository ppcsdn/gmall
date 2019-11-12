package com.atgg.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.bean.PmsProductSaleAttr;
import com.atgg.gmall.bean.PmsSkuInfo;
import com.atgg.gmall.bean.PmsSkuSaleAttrValue;
import com.atgg.gmall.service.SkuService;
import com.atgg.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pstart
 * @create 2019-10-15 10:04
 */
@Controller
public class ItemController {
    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")//ModelMap中的数据用来渲染thymeleaf前端的页面
    public String item(@PathVariable String skuId, ModelMap skuAndSpuMap) {
        PmsSkuInfo pmsSkuInfo = skuService.item(skuId);
        String productId = pmsSkuInfo.getProductId();
        //该处由sql实现,属性和属性值列表,isChecked 1与0由sql判断是否存在sku_value_id,存在即选中
        List<PmsProductSaleAttr> pmsProductSaleAttr = spuService.spuSaleAttrListBySql(productId, skuId);
        skuAndSpuMap.put("skuInfo", pmsSkuInfo);
        skuAndSpuMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttr);

        //在加载item的时候,下载一个json文件,用来保存sku信息
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(productId);
        Map<String, String> skuMap = new HashMap<>();
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String skuId1 = skuInfo.getId();//V 根据属性值id的拼接做key唯一去找skuid
            List<PmsSkuSaleAttrValue> skuAttrValueList = skuInfo.getSkuSaleAttrValueList();
            String attr_value = "";//K
            for (PmsSkuSaleAttrValue pmsSkuAttrValue : skuAttrValueList) {
                attr_value = attr_value+"|"+pmsSkuAttrValue.getSaleAttrValueId();
            }
            skuMap.put(attr_value, skuId1);
        }
        String skuJson = JSON.toJSONString(skuMap);
        skuAndSpuMap.put("skuJson", skuJson);
//        skuAndSpuMap.put("spu","spu_"+productId+".json");

        return "item";
    }

    @RequestMapping("testThymeleaf")
    public String testThymeleaf(ModelMap thymeleafMap) {
        String hello = "hello thymeleaf!";
        List<String> list = new ArrayList();

        for (int i = 0; i < 5; i++) {
            list.add("循环元素" + i);
        }
        thymeleafMap.put("hello", hello);
        thymeleafMap.put("list", list);
        thymeleafMap.put("testing", "点我");

        return "testThymeleaf";
    }

}
