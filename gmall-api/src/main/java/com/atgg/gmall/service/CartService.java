package com.atgg.gmall.service;

import com.atgg.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-24 3:10
 */
public interface CartService {
    void updateCart(OmsCartItem omsCartItemFromDb);

    void addCart(OmsCartItem omsCartItems);

    OmsCartItem isCartExits(String userId, OmsCartItem omsCartItem);

    List<OmsCartItem> cartList(String userId);

    void updateCartByUserId(OmsCartItem omsCartItem);

    List<OmsCartItem> getCartListByUserId(String userId);

    void delCartList(List<String> delCartListIds, String userId);

    boolean getPriceAndQuantityFromMysql(OmsCartItem omsCartItem);

}
