package com.atgg.gmall.manage.mapper;

import com.atgg.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-12 17:41
 */
public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
    List<PmsProductSaleAttr> spuSaleAttrListBySql(@Param("spuId") String productId, @Param("skuId") String skuId);

}
