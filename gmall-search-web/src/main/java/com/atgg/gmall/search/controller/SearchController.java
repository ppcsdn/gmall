package com.atgg.gmall.search.controller;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.HashSet;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.bean.*;
import com.atgg.gmall.service.AttrService;
import com.atgg.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-18 11:42
 */
@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("index")
    public String index() {
        return "index";
    }

    @RequestMapping("list")
    public String search(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfo = searchService.search(pmsSearchParam);//查的es
        if (pmsSearchSkuInfo != null && pmsSearchSkuInfo.size() > 0) {
            HashSet<String> valueIds = new HashSet<>();//base_attr_value的
            for (PmsSearchSkuInfo searchSkuInfo : pmsSearchSkuInfo) {
                List<PmsSkuAttrValue> skuAttrValueList = searchSkuInfo.getSkuAttrValueList();
                for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                    String valueId = pmsSkuAttrValue.getValueId();
                    valueIds.add(valueId);
                }
            }

            List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueIds(valueIds);//该集合包含了平台所有的基础属性值的集合id
            String[] valueUser = pmsSearchParam.getValueId();//用户选中的平台的基本属性id
            //对用户选中了的多个的平台的基础属性(根据用户选中的平台基础属性id) 的移除,使用游标
            if (valueUser != null && valueUser.length > 0) {
                //面包屑
                List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();
                for (String valueId : valueUser) {
                    Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                    while (iterator.hasNext()) {//有继续,没有-1跳出for循环
                        PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                        for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrInfo.getAttrValueList()) {
                            if (valueId.equals(pmsBaseAttrValue.getId())) {//在用户选中的同时制作了面包屑
                                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();//对用户已经选中的属性url不做拼接,根据valueId判断
                                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, valueId));
                                pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                                pmsSearchCrumb.setValueId(valueId);
                                pmsSearchCrumbList.add(pmsSearchCrumb);
                                iterator.remove();
                            }
                        }
                    }
                }
                modelMap.put("attrValueSelectedList", pmsSearchCrumbList);
            }
            modelMap.put("attrList", pmsBaseAttrInfos);

//            List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();
//            if (valueUser != null && valueUser.length > 0) {
//                for (String valueId : valueUser) {
//                    PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();//对用户已经选中的属性url不做拼接,根据valueId判断
//                    pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, valueId));
//                    pmsSearchCrumb.setValueName(valueId);
//                    pmsSearchCrumb.setValueId(valueId);
//                    pmsSearchCrumbList.add(pmsSearchCrumb);
//                }
//                modelMap.put("attrValueSelectedList", pmsSearchCrumbList);
//            }

        }

        modelMap.put("skuLsInfoList", pmsSearchSkuInfo);
        //拼接用户输入的具体参数每一个都作为独立的url
        modelMap.put("urlParam", getUrlParam(pmsSearchParam));


        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam, String... valueIdForDelete) {
        String urlParam = "";
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
        if (StringUtils.isNotBlank(urlParam)) {//当urlParam第一次拼接时为空字符串加“&”,所以做预判断
            urlParam = urlParam + "&";//url不为空时直接加上&
        } else {
            if (StringUtils.isNotBlank(catalog3Id)) {//url为空时代表前面的值为问号？
                urlParam = urlParam + "catalog3Id=" + catalog3Id;
            }
            if (StringUtils.isNotBlank(keyword)) {
                urlParam = urlParam + "keyword=" + keyword;
            }
        }

        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                if (valueIdForDelete == null || valueIdForDelete.length == 0) {
                    urlParam = urlParam + "&valueId=" + valueId;
                } else {
                    if (!valueIdForDelete[0].equals(valueId)) {//该处为面包屑的url,为自己的就不用拼接url了
                        urlParam = urlParam + "&valueId=" + valueId;
                    }
                }
            }
        }
        return urlParam;
    }


//    private String getUrlParam(PmsSearchParam pmsSearchParam) {
//        String urlParam = "";
//        String catalog3Id = pmsSearchParam.getCatalog3Id();
//        String keyword = pmsSearchParam.getKeyword();
//        String[] valueIds = pmsSearchParam.getValueId();
//        if (StringUtils.isNotBlank(urlParam)) {//当urlParam第一次拼接时为空字符串加“&”,所以做预判断
//            urlParam = urlParam + "&";//url不为空时直接加上&
//        } else {
//            if (StringUtils.isNotBlank(catalog3Id)) {//url为空时代表前面的值为问号？
//                urlParam = urlParam + "catalog3Id=" + catalog3Id;
//            }
//            if (StringUtils.isNotBlank(keyword)) {
//                urlParam = urlParam + "keyword=" + keyword;
//            }
//        }
//        if (valueIds != null && valueIds.length > 0) {//该处为用户进入具体商品后显示的基础的平台属性,直接拼接就行
//            for (String valueId : valueIds) {
//                urlParam = urlParam + "&valueId=" + valueId;
//            }
//        }
//        return urlParam;
//    }
}
