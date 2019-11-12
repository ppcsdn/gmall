package com.atgg.gmall.cart.util;

import java.math.BigDecimal;
import java.util.List;

import com.atgg.gmall.bean.OmsCartItem;

/**
 * @author Pstart
 * @create 2019-10-24 12:52
 */
public class PriceUtil {

    public static BigDecimal getTotalAmout(List<OmsCartItem> omsCartItems) {
        BigDecimal amoutPrice = new BigDecimal("0");//计算商品总价格
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")){
                amoutPrice = amoutPrice.add(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            }
        }
        return amoutPrice;
    }
}
