package com.atgg.gmall.service;

import com.atgg.gmall.bean.PmsBaseSaleAttr;
import com.atgg.gmall.bean.PmsProductImage;
import com.atgg.gmall.bean.PmsProductInfo;
import com.atgg.gmall.bean.PmsProductSaleAttr;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-12 14:33
 */
public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListBySql(String productId, String skuId);
}
