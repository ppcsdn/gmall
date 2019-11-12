package com.atgg.gmall.payment.service.impl;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atgg.gmall.bean.PaymentInfo;
import com.atgg.gmall.payment.mapper.PaymentInfoMapper;
import com.atgg.gmall.service.PaymentService;
import com.atgg.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void save(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void update(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);

        example.createCriteria().andEqualTo("orderSn", paymentInfo.getOrderSn());
        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
    }

    @Override//通过消息队列发送支付成功消息，订单系统中订单表信息的更新
    public void sendPaySuccessQueue(PaymentInfo paymentInfo) {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            //开启事务
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue pay_success_queue = session.createQueue("PAY_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(pay_success_queue);//消息提供者
            ActiveMQMapMessage map = new ActiveMQMapMessage();//发送消息
            map.setString("out_trade_no", paymentInfo.getOrderSn());
            map.setString("status", "success");
            producer.send(map);
            session.commit();
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendPaCheckQueue(PaymentInfo paymentInfo, long count) {
        //根据订单号out_trade_no发送延迟检查支付状态队列
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            //开启事务
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue pay_success_queue = session.createQueue("PAY_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(pay_success_queue);//消息提供者
            ActiveMQMapMessage map = new ActiveMQMapMessage();//发送消息
            map.setString("out_trade_no", paymentInfo.getOrderSn());
            map.setLong("count", count);
            map.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 25);//设置延迟队列的延迟时间,对应activeMq的schedulerSupport="true"配置
            producer.send(map);
            session.commit();
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> checkPay(String out_trade_no) {
        // 调用支付宝接口，根据out_trade_no检查支付状态
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("out_trade_no", out_trade_no);
        request.setBizContent(JSON.toJSONString(mapParam));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        Map<String, Object> map = new HashMap<>();
        if (response.isSuccess()) {
            System.out.println("调用成功");// 交易已创建
            map.put("status", response.getTradeStatus());
            map.put("trade_no", response.getTradeNo());
            map.put("callback_content", JSON.toJSONString(response));
        } else {
            map.put("status", "fail");
            System.out.println("调用失败");// 交易未创建
        }

        return map;
    }

    @Override
    public String checkPayStatus(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(out_trade_no);
        return paymentInfoMapper.selectOne(paymentInfo).getPaymentStatus();
    }
}
