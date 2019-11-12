package com.atgg.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.bean.PmsBaseSaleAttr;
import com.atgg.gmall.bean.PmsProductImage;
import com.atgg.gmall.bean.PmsProductInfo;
import com.atgg.gmall.bean.PmsProductSaleAttr;
import com.atgg.gmall.manage.util.MyUploadUtil;
import com.atgg.gmall.service.SpuService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-12 14:23
 */
@RestController
@CrossOrigin
public class SpuController {
    @Reference
    SpuService spuService;

    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id) {
        return spuService.spuList(catalog3Id);
    }

    @RequestMapping("baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return spuService.baseSaleAttrList();
    }

    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {

        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    //@RequestParam("file") 获得文件上传的数据
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        //静态方法在程序开始时就加载进内存了,上传文件返回url地址值
        String imgUrl = MyUploadUtil.upload_image(multipartFile);
        return imgUrl;

    }

    @RequestMapping("spuImageList")
    public List<PmsProductImage> spuImageList(String spuId) {
        return spuService.spuImageList(spuId);
    }
    @RequestMapping("spuSaleAttrList")//联合主键productId(spu_id),sale_attr_id,PmsProductSaleAttrValue的
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        return spuService.spuSaleAttrList(spuId);
    }

}
