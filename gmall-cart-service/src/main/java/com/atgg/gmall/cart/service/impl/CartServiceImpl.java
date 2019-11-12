package com.atgg.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.bean.OmsCartItem;
import com.atgg.gmall.cart.mapper.OmsCartItemMapper;
import com.atgg.gmall.service.CartService;
import com.atgg.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Pstart
 * @create 2019-10-24 3:12
 */
@Service//注入容器暴露自己的方法提供服务
public class CartServiceImpl implements CartService {
    @Autowired
    OmsCartItemMapper omsCartItemMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example example = new Example(OmsCartItem.class);//创建example实例，封装条件,指定要操作的资源类
        //获取用户添加相同商品后的参数，update进sql,启动debug查看sql语句
        example.createCriteria().andEqualTo("id", omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb, example);//执行修改
        //set进redis缓存，同步缓存,当用户再刷新查看购物车信息时访问缓存
        Jedis jedis = redisUtil.getJedis();
        jedis.hset("user:" + omsCartItemFromDb.getMemberId() + ":cart", omsCartItemFromDb.getProductSkuId(), JSON.toJSONString(omsCartItemFromDb));
        jedis.close();
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        omsCartItemMapper.insertSelective(omsCartItem);

        Jedis jedis = redisUtil.getJedis();
        jedis.hset("user:" + omsCartItem.getMemberId() + ":cart", omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
        jedis.close();
    }

    @Override
    public OmsCartItem isCartExits(String userId, OmsCartItem omsCartItem) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setMemberId(userId);//查询用户登录情况下购物车里是否有商品
        cartItem.setProductSkuId(omsCartItem.getProductSkuId());

        return omsCartItemMapper.selectOne(cartItem);
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {//查询用户购物车信息的缓存并回现，对于失效宝贝查询mysql遍历结果并同步到redis缓存中
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("user:" + userId + ":cart");//查询缓存中用户购物车数据
        if (hvals != null && hvals.size() > 0) {//有缓存直接回显
            for (String hval : hvals) {
                OmsCartItem cartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(cartItem);
            }
        } else {//没缓存查询到sql并redis同步，redis出现问题被意外删除或者过期了，例如购物车中的失效宝贝
            OmsCartItem cartItem = new OmsCartItem();
            cartItem.setMemberId(userId);
            omsCartItems = omsCartItemMapper.select(cartItem);//去数据库拿到用户的所有购物车信息并缓存
            if (omsCartItems != null && omsCartItems.size() > 0) {
                Map<String, String> map = new HashMap<>();
                for (OmsCartItem omsCartItem : omsCartItems) {
                    map.put(omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
                }
                jedis.hmset("user:" + userId + ":cart", map);//同步进缓存中key-value
            }

        }
        jedis.close();
        return omsCartItems;
    }

    @Override
    public void updateCartByUserId(OmsCartItem omsCartItem) {

        Example example = new Example(OmsCartItem.class);//创建example实例，封装条件,指定要操作的资源类
        //根据联合唯一id匹配唯一的数据
        example.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productSkuId", omsCartItem.getProductSkuId());

        OmsCartItem omsCartItem2 = new OmsCartItem();
        omsCartItem2.setIsChecked(omsCartItem.getIsChecked());//修改资源类的选中状态
        omsCartItemMapper.updateByExampleSelective(omsCartItem2, example);//执行修改,选中状态值在sql中被修改

        //同步缓存
        Jedis jedis = redisUtil.getJedis();
        String hget = jedis.hget("user:" + omsCartItem.getMemberId() + ":cart", omsCartItem.getProductSkuId());//根据联合唯一id匹配
        OmsCartItem omsCartItem1 = JSON.parseObject(hget, OmsCartItem.class);//将缓存的数据转化成OmsCartItem
        omsCartItem1.setIsChecked(omsCartItem.getIsChecked());//修改资源类的选中状态更新缓存
        jedis.hset("user:" + omsCartItem.getMemberId() + ":cart", omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem1));
        jedis.close();

    }

    @Override
    public List<OmsCartItem> getCartListByUserId(String userId) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("user:" + userId + ":cart");//查询缓存中用户购物车数据
        if (hvals != null && hvals.size() > 0) {//有缓存直接回显
            for (String hval : hvals) {
                OmsCartItem cartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(cartItem);
            }
        }
        jedis.close();
        return omsCartItems;
    }

    @Override
    public void delCartList(List<String> delCartListIds, String userId) {
        //数据库与缓存中购物车数据数目一致
        Jedis jedis = redisUtil.getJedis();
        if (delCartListIds != null) {
            for (String delCartListId : delCartListIds) {
                OmsCartItem cartItem = new OmsCartItem();
                cartItem.setMemberId(userId);
                cartItem.setProductSkuId(delCartListId);
                omsCartItemMapper.delete(cartItem);
                jedis.hdel("user:" + userId + ":cart", delCartListId);
            }
        }
        jedis.close();
    }


    @Override
    public boolean getPriceAndQuantityFromMysql(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);//创建example实例，封装条件,指定要操作的资源类
        //根据联合唯一id匹配唯一的数据，获得mysql中的价格和数量信息与缓存中的数据做一致性校验，不一致表示商品失效
        example.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productSkuId", omsCartItem.getProductSkuId());
        List<OmsCartItem> omsCartItems = omsCartItemMapper.selectByExample(example);
        Jedis jedis = redisUtil.getJedis();
        String hget = jedis.hget("user:" + omsCartItem.getMemberId() + ":cart", omsCartItem.getProductSkuId());
        OmsCartItem omsCartItemRedis = JSON.parseObject(hget, OmsCartItem.class);
        BigDecimal quantity = omsCartItemRedis.getQuantity();//-1 0 1 小于,等于,大于
        BigDecimal price = omsCartItemRedis.getPrice();
        BigDecimal quantity1 = omsCartItems.get(0).getQuantity();
        BigDecimal price1 = omsCartItems.get(0).getPrice();
        if (quantity.compareTo(quantity1) == 0 && price.compareTo(price1) == 0) {
            return true;
        }
        jedis.close();
        return false;
    }
}
