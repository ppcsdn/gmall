package com.atgg.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atgg.gmall.bean.*;
import com.atgg.gmall.manage.mapper.*;
import com.atgg.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-12 14:35
 */
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);

        return pmsProductInfoMapper.select(pmsProductInfo);
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }


    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {//拿到整个表单的数据封装到一个实体类pmsProductInfo
        // 然后从pmsProductInfo实体类中获得销售属性的集合,再从销售属性集合中取销售属性值的集合
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
        String pmsProductInfoId = pmsProductInfo.getId();//该处id值作为联合主键使用

        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {
            //保存pmsProductInfo对方表中的字段进行关联,为以后的修改和删除作必要的准备
            //该处字段pmsProductSaleAttr是平台提供给商户的可选属性
            pmsProductSaleAttr.setProductId(pmsProductInfoId);
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            //该处字段pmsProductSaleAttrValue是商户的选择具体的属性记录,通过与pmsProductInfoId字段组成联合主键
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : spuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(pmsProductInfoId);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }

        }

        //保存商品图片信息
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        for (PmsProductImage pmsProductImage : spuImageList) {
            pmsProductImage.setProductId(pmsProductInfoId);
            pmsProductImageMapper.insertSelective(pmsProductImage);
        }
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        return pmsProductImageMapper.select(pmsProductImage);
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {

        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        //pmsProductSaleAttrValue必须通过联合主键查询
        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValueList);
        }
        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListBySql(String productId, String skuId) {
        return pmsProductSaleAttrMapper.spuSaleAttrListBySql(productId,skuId);
    }


}
