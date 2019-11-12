package com.atgg.gmall.service;

import com.atgg.gmall.bean.UmsMember;
import com.atgg.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-11 18:30
 */
public interface UserService {
    UmsMember getUserById(String memberId);
    List<UmsMember> getAllUser();

    UmsMember login(UmsMember umsMember);

    void setUserTokenToCache(String token, String id);

    UmsMember veryfyToken(String token);

    UmsMember getUserFromCacheById(String userId);

    UmsMemberReceiveAddress getAddressById(String addressId);
}
