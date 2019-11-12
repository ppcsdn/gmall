package com.atgg.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.bean.PmsSkuAttrValue;
import com.atgg.gmall.bean.PmsSkuImage;
import com.atgg.gmall.bean.PmsSkuInfo;
import com.atgg.gmall.bean.PmsSkuSaleAttrValue;
import com.atgg.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atgg.gmall.manage.mapper.PmsSkuImageMapper;
import com.atgg.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atgg.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atgg.gmall.service.SkuService;
import com.atgg.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Pstart
 * @create 2019-10-14 19:53
 */
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();//另外3个表根据该id遍历数据逐级保存数据入库，json格式数据
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
    }

    @Override
    public PmsSkuInfo item(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //请求缓存
        Jedis jedis = redisUtil.getJedis();
        try {
            //缓存规则，定义缓存key和value,sku:1314:info
            String skuInfoJson = jedis.get("skuInfo:" + skuId + ":info");
            if (StringUtils.isBlank(skuInfoJson)) {//为空白成立
                //获得分布式锁,及解锁,应对高并发情况使用分布式锁,防止缓存击穿接收高并发的请求后直接操作了数据库
                String uuid = UUID.randomUUID().toString();
                String OK = jedis.set("skuInfo:" + skuId + " :lock", uuid, "nx", "px", 10000);//设置过期时间
                if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                    //查询db
                    pmsSkuInfo = itemFromDb(skuId);
                    //查询有结果则更新到redis中
                    if (pmsSkuInfo != null) {
                        jedis.set("skuInfo:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                    }
                    // 删除分布式锁
//                    String s = jedis.get("skuInfo:" + skuId + ":lock");
//                    if(StringUtils.isNotBlank(s)&&s.equals(uuid)){
//                        jedis.del("skuInfo:" + skuId + ":lock");
//                    }

                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    Object eval = jedis.eval(script, Collections.singletonList("skuInfo:" + skuId + ":lock"), Collections.singletonList(uuid));

                } else {//分布式锁设置失败 开始自旋
                    return item(skuId);
                }

            } else {
                //转换缓存
                pmsSkuInfo = JSON.parseObject(skuInfoJson, PmsSkuInfo.class);
            }
        } finally {
            jedis.close();
        }
        return pmsSkuInfo;


    }

    public PmsSkuInfo itemFromDb(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);

        pmsSkuInfo1.setSkuImageList(pmsSkuImages);
        return pmsSkuInfo1;
    }

    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        return pmsSkuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(productId);
    }

    //查询表中所有数据插入es,也就是skuinfo表中的内容和SkuAttrValue表中内容的所有id
    @Override
    public List<PmsSkuInfo> getSkuInfo() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfos;
    }

    public PmsSkuInfo getSkuInfoById(String productSkuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        return pmsSkuInfoMapper.selectOne(pmsSkuInfo);
    }

    @Override
    public boolean checkPrice(BigDecimal price, String productSkuId) {
        boolean b = false;
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        if (pmsSkuInfo1.getPrice().compareTo(price) == 0) {
            b = true;
        }
        return b;
    }
}
