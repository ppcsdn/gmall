package com.atgg.gmall.payment;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class Boss {

    public static void main(String[] args) {
        topic();
    }

    public static void topic(){

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Topic topic = session.createTopic("boss meetting");// 话题名称

            MessageProducer producer = session.createProducer(topic);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("为尚硅谷的伟大复兴而努力奋斗！");// 消息内容
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public static void queue(){

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("boss drinking");// 队列名称

            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("我渴了，我想喝杯水");// 消息内容
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
