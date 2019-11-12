package com.atgg.gmall.service;

import com.atgg.gmall.bean.PaymentInfo;

import java.util.Map;

/**
 * @author Pstart
 * @create 2019-11-01 21:17
 */
public interface PaymentService {
    void save(PaymentInfo paymentInfo);

    void update(PaymentInfo paymentInfo);

    void sendPaySuccessQueue(PaymentInfo paymentInfo);

    void sendPaCheckQueue(PaymentInfo paymentInfo, long l);

    Map<String, Object> checkPay(String out_trade_no);

    String checkPayStatus(String out_trade_no);

}
