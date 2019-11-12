package com.atgg.gmall.order.service.impl;

import java.util.Collections;
import java.util.List;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.bean.OmsCartItem;
import com.atgg.gmall.bean.OmsOrder;
import com.atgg.gmall.bean.OmsOrderItem;
import com.atgg.gmall.order.mapper.OmsOrderItemMapper;
import com.atgg.gmall.order.mapper.OmsOrderMapper;
import com.atgg.gmall.service.OrderService;
import com.atgg.gmall.util.ActiveMQUtil;
import com.atgg.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

/**
 * @author Pstart
 * @create 2019-10-29 14:42
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItem.setOrderSn(omsOrder.getOrderSn());
            omsOrderItemMapper.insertSelective(omsOrderItem);
        }
    }

    @Override
    public void saveTradeCode(String userId, String tradeCode) {
        Jedis jedis = redisUtil.getJedis();//设置kv过期时间,订单在30分钟后过期
        jedis.setex("user:" + userId + ":tradeCode", 60 * 30, tradeCode);
        jedis.close();
    }


    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        boolean b = false;
        Jedis jedis = redisUtil.getJedis();
        // 使用lua脚本防止并发攻击
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long i = (Long) jedis.eval(script, Collections.singletonList("user:" + userId + ":tradeCode"),
                Collections.singletonList(tradeCode));//拿到缓存中的value与参数中的value比较,一旦一致立马删除
        if (i != 0) {
            b = true;
        }
        jedis.close();
        return b;
    }

    @Override
    public OmsOrder getOrderByUserId(String userId, String orderSn) {

        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(orderSn);
        omsOrder.setMemberId(userId);

        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    public OmsOrder getOrderByOrderSn(String orderSn) {

        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(orderSn);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);

        OmsOrderItem omsOrderItem = new OmsOrderItem();
        omsOrderItem.setOrderSn(orderSn);
        List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItem);
        omsOrder1.setOmsOrderItems(select);

        return omsOrder1;


    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());
        omsOrderMapper.updateByExampleSelective(omsOrder, example);
    }

    @Override//通过消息队列发送订单成功队列,库存消费,锁定库存
    public void sendOrderPay(OmsOrder omsOrder) {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            //开启事务
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue pay_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(pay_success_queue);//消息提供者
            ActiveMQTextMessage text = new ActiveMQTextMessage();

            String orderSn = omsOrder.getOrderSn();

            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(orderSn);
            OmsOrder omsOrderResult = omsOrderMapper.selectOne(omsOrderParam);

            OmsOrderItem omsOrderItemParam = new OmsOrderItem();
            omsOrderItemParam.setOrderSn(omsOrderResult.getOrderSn());
            List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItemParam);

            omsOrderResult.setOmsOrderItems(omsOrderItems);

            text.setText(JSON.toJSONString(omsOrderResult));
            producer.send(text);
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

//    //加分布式,对拿到相同tradeCode的并发操作加锁
//    @Override
//    public boolean checkTradeCode(String userId, String tradeCode) {
//        Jedis jedis = redisUtil.getJedis();
//        try {
//            String oldTrade = jedis.get("user:" + userId + ":tradeCode");
//            //set无序不可重复,获得分布式锁,及解锁,应对高并发情况使用分布式锁,防止缓存击穿接收高并发的请求后直接操作了数据库
//            String OK = jedis.set("tradeInfo:" + userId + " :lock", tradeCode, "nx", "px", 2000);//设置过期时间
//            if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {//设置分布式锁成功才能校验tradeCode,分布式锁设置失败即开始自旋
//                if (tradeCode.equals(oldTrade)) {//比较一致并删除缓存中的tradeCode,即防止表单重复提交
//                    jedis.del("user:" + userId + ":tradeCode");
//                    return true;
//                }//处理过程完成,或者分布式锁过期释放了,使用lua脚本删除分布式锁,注意引用不可达的返回return
//                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//                Object eval = jedis.eval(script, Collections.singletonList("tradeInfo:" + userId + " :lock"), Collections.singletonList(tradeCode));
//            } else {//分布式锁设置失败,自旋等待并发用户的过期时间到了再放行另外一个并发用户
//                return checkTradeCode(userId, tradeCode);
//            }
//            return false;
//        } finally {
//            jedis.close();
//        }
//    }

}
