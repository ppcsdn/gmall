package com.atgg.gmall.service;

import com.atgg.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-14 19:53
 */
public interface SkuService {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo item(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getSkuInfo();

    PmsSkuInfo getSkuInfoById(String productSkuId);

    boolean checkPrice(BigDecimal price, String productSkuId);
}
