package com.atgg.gmall.manage.mapper;
import com.atgg.gmall.bean.PmsSkuInfo;
import com.atgg.gmall.bean.PmsSkuSaleAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


/**
 * @author Pstart
 * @create 2019-10-14 20:00
 */
public interface PmsSkuSaleAttrValueMapper extends Mapper<PmsSkuSaleAttrValue> {
   List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(@Param("spuId") String productId);

}
