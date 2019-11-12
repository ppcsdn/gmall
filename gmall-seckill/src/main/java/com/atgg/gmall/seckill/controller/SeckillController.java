package com.atgg.gmall.seckill.controller;

import com.atgg.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Pstart
 * @create 2019-11-06 19:54
 */
@Controller
public class SeckillController {
    @Autowired
    RedisUtil redisUtil;

    @RequestMapping("killone")
    @ResponseBody
    public Long killone(HttpServletRequest request) {
        String userId = "";

        Jedis jedis = redisUtil.getJedis();//设置分布式锁,对已经存在的用户记录锁定10秒
        String OK = jedis.set("user:" + userId + "seckill", "1", "nx", "px", 1000 * 10);
        jedis.watch("stock");//监听,一旦发现有在execute前被动过的key,事务取消返回nil-reply
        Long stock = Long.parseLong(jedis.get("stock"));
        if (true) {
//        if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
            Transaction multi = jedis.multi();//开启事务,execute操作前动作放进队列
            if (stock > 0) {
                multi.incrBy("stock", -1);//操作库存数-1
                List<Object> exec = multi.exec();//执行
                if (exec.size() > 0) {
                    System.out.println("抢购成功，当前库存剩余数量" + exec.get(0));
                    jedis.set("user:" + userId + ":seckill", "1", "nx", "px", 1000 * 60 * 15);// 每个抢购成功的用户15每分钟内不能再抢
                    // 抢到库存后发消息，生成订单
                } else {
                    System.out.println(request.getRemoteAddr() + ":非洲人抢不到@.@");
                }
            } else {
                System.out.println("抢购失败，活动结束，当前库存剩余数量" + stock);
            }

        }
        jedis.close();
        return stock;
    }
}

