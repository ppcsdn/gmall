package com.atgg.gmall.user.service.impl;

import java.awt.Desktop.Action;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.bean.UmsMember;
import com.atgg.gmall.bean.UmsMemberReceiveAddress;
import com.atgg.gmall.service.UserService;
import com.atgg.gmall.user.mapper.UmsMemberMapper;
import com.atgg.gmall.user.mapper.UserMemberReceiveAddresMapper;
import com.atgg.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UmsMemberMapper umsMemberMapper;

    @Autowired
    UserMemberReceiveAddresMapper userMemberReceiveAddresMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public UmsMember getUserById(String memberId) {
        //UmsMember umsMember = umsMemberMapper.selectUserById(memberId);
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setId(memberId);
        UmsMember umsMember2 = umsMemberMapper.selectOne(umsMember1);
        return umsMember2;
    }

    @Override
    public List<UmsMember> getAllUser() {

        return umsMemberMapper.selectAll();
    }


    public List<UmsMemberReceiveAddress> getAddressByUserId(String userId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(userId);
        return userMemberReceiveAddresMapper.select(umsMemberReceiveAddress);
    }

    @Override
    public UmsMember login(UmsMember umsMember) {//登录直接获取用户（地址）所有信息并写入缓存
        UmsMember umsMemberInfo = new UmsMember();
        umsMemberInfo.setUsername(umsMember.getUsername());
        umsMemberInfo.setPassword(umsMember.getPassword());
        UmsMember umsMemberInfos = umsMemberMapper.selectOne(umsMemberInfo);

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = getAddressByUserId(umsMemberInfos.getId());//根据memberid获取地址
        umsMemberInfos.setUmsMemberReceiveAddresses(umsMemberReceiveAddresses);

        //存入缓存
        Jedis jedis = redisUtil.getJedis();//token数据为a.b.c形式字符串形式serverKey, map,浏览器盐值ip
        jedis.setex("user:" + umsMemberInfos.getId() + ":info", 60 * 60 * 2, JSON.toJSONString(umsMemberInfos));
        jedis.close();

        return umsMemberInfos;
    }

    @Override
    public void setUserTokenToCache(String token, String id) {
        UmsMember umsMember = new UmsMember();
        umsMember.setId(id);
        UmsMember umsMember1 = umsMemberMapper.selectOne(umsMember);

        Jedis jedis = redisUtil.getJedis();//token数据为a.b.c形式字符串形式serverKey, map,浏览器盐值ip
        jedis.setex("user:" + token + ":token", 60 * 60 * 2, JSON.toJSONString(umsMember1));
        jedis.close();
    }

    @Override
    public UmsMember veryfyToken(String token) {//
        UmsMember umsMember = null;
        Jedis jedis = redisUtil.getJedis();//token数据为a.b.c形式字符串形式serverKey, map,浏览器盐值ip
        String userJson = jedis.get("user:" + token + ":token");
        if (StringUtils.isNotBlank(userJson)) {//取出缓存中的UmsMember数据转换成UmsMember
            umsMember = JSON.parseObject(userJson, UmsMember.class);
        }

        jedis.close();
        return umsMember;
    }

    @Override
    public UmsMember getUserFromCacheById(String userId) {

        UmsMember umsMember = null;
        //存入缓存
        Jedis jedis = redisUtil.getJedis();//token数据为a.b.c形式字符串形式serverKey, map,浏览器盐值ip
        String redisJson = jedis.get("user:" + userId + ":info");
        if (StringUtils.isNotBlank(redisJson)) {
            umsMember = JSON.parseObject(redisJson, UmsMember.class);
        }
        jedis.close();
        return umsMember;
    }

    @Override
    public UmsMemberReceiveAddress getAddressById(String addressId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(addressId);
        return userMemberReceiveAddresMapper.selectOne(umsMemberReceiveAddress);
    }
}
