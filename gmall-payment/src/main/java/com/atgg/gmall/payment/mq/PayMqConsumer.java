package com.atgg.gmall.payment.mq;

import com.atgg.gmall.bean.PaymentInfo;
import com.atgg.gmall.service.PaymentService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class PayMqConsumer {

    @Autowired
    PaymentService paymentService;

    //创建监听器获取pull消息
    @JmsListener(containerFactory = "jmsQueueListener", destination = "PAY_CHECK_QUEUE")
    public void orderPayConsum(ActiveMQMapMessage activeMQMapMessage) throws JMSException {
        String out_trade_no = activeMQMapMessage.getString("out_trade_no");
        Long count = activeMQMapMessage.getLong("count");//延迟队列的使用次数
        //调用支付宝API检查改生成的支付订单out_trade_no的支付状态,万金油类型法us
        Map<String, Object> map = new HashMap<>();
        map = paymentService.checkPay(out_trade_no);

        String status = (String) map.get("status");
        if (status.equals("TRADE_SUCCESS")||status.equals("TRADE_FINISHED")) {
            // 根据返回的检查结果
            // 更新payment
            // 发送支付成功消息
            //更新支付信息业务
            String pay_status = paymentService.checkPayStatus(out_trade_no);//查PaymentInfo表预判断,已经支付的订单不再执行
            if (!pay_status.equals("已支付")){//消费提供者从调用支付宝api拿到的支付结果，在此消费
                // 更新支付信息业务
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String)map.get("trade_no"));
                paymentInfo.setCallbackContent((String) map.get("callback_content"));
                paymentInfo.setCallbackTime(new Date());

                paymentService.update(paymentInfo);//支付详情信息表,与使用消息队列并发,互不干扰
                // 更新订单信息业务等其他系统业务
                // 发送系统消息队列，通知gmall系统某outTradeNo已经支付成功
                paymentService.sendPaySuccessQueue(paymentInfo);//更新订单表的支付状态
            }

            System.out.println("检查已经支付，调用支付服务，进行后续系统处理");

        } else {
            // 检查不成功
            // 再次发送延迟队列
            if (count > 0) {
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                count--;
                paymentService.sendPaCheckQueue(paymentInfo, count);
                System.out.println("再次发送检查队列，次数剩余" + count + "次");
            } else {
                System.out.println("次数耗尽，停止检查");
            }
        }

        System.out.println("订单消费PAY_SUCCESS_QUEUE队列");
    }

}
