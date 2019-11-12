package com.atgg.gmall.service;

import com.atgg.gmall.bean.OmsCartItem;
import com.atgg.gmall.bean.OmsOrder;

/**
 * @author Pstart
 * @create 2019-10-29 14:39
 */
public interface OrderService {

    void saveOrder(OmsOrder omsOrder);

    void saveTradeCode(String userId, String tradeCode);

    boolean checkTradeCode(String userId, String tradeCode);

    OmsOrder getOrderByUserId(String userId, String orderSn);

    OmsOrder getOrderByOrderSn(String orderSn);

    void updateOrder(OmsOrder omsOrder);

    void sendOrderPay(OmsOrder omsOrder);
}
