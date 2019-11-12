package com.atgg.gmall.payment.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atgg.gmall.bean.OmsOrder;
import com.atgg.gmall.bean.PaymentInfo;
import com.atgg.gmall.cart.annotations.LoginRequired;
import com.atgg.gmall.payment.config.AlipayConfig;
import com.atgg.gmall.payment.util.HttpClient;
import com.atgg.gmall.service.OrderService;
import com.atgg.gmall.service.PaymentService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Pstart
 * @create 2019-10-29 12:13
 */
@Controller
public class PayController {
    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService;


    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;

    @RequestMapping("wx/submit")
    @ResponseBody
    public Map createNative(String orderSn) {
        // 做一个判断：支付日志中的订单支付状态 如果是已支付，则不生成二维码直接重定向到消息提示页面！
        // 调用服务层数据
        // 第一个参数是订单Id ，第二个参数是多少钱，单位是分
        if (orderSn.length() > 32) {
            orderSn = orderSn.substring(30);
        }
        Map map = createNative(orderSn + "", "1");
        System.out.println(map.get("code_url"));
        // data = map
        return map;
    }


    public Map createNative(String orderId, String total_fee) {
        //1.创建参数
        Map<String, String> param = new HashMap();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", orderId);//商户订单号
        param.put("total_fee", total_fee);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", " http://2z72m78296.wicp.vip/wx/callback/notify");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no", orderId);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @RequestMapping("alipay/callback/return")
    @LoginRequired
    public String callbackReturn(HttpServletRequest request, ModelMap modelMap) {
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_no = request.getParameter("trade_no");
        String total_amount = request.getParameter("total_amount");
        String sign = request.getParameter("sign");// 签名

        //同步查看签名,如果异步通知,不需要封装map
        Map<String, String> map = new HashMap<>();
        map.put("sign", sign);
        boolean b = false;
        try {
            b = AlipaySignature.rsaCheckV1(map, AlipayConfig.alipay_public_key, AlipayConfig.charset);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //幂等性校验f(f(x))=f(x)，该等式成立时不再执行程序,即延迟队列调用支付宝接口回传已支付信息时修改oms_order表信息后
        String pay_status = paymentService.checkPayStatus(out_trade_no);
        if (!pay_status.equals("已支付")) {//预判断
            // 更新支付信息业务
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(request.getQueryString());
            paymentInfo.setCallbackTime(new Date());

            paymentService.update(paymentInfo);//支付详情信息表,与使用消息队列并发,互不干扰
            // 更新订单信息业务等其他系统业务
            // 发送系统消息队列，通知gmall系统某outTradeNo已经支付成功
            paymentService.sendPaySuccessQueue(paymentInfo);//更新订单表的支付状态
        }

        return "redirect:/finish.html";
    }


    @RequestMapping("alipay/submit")//选择支付宝支付,调用alipay提供的SDK方法生成支付表单
    @LoginRequired
    @ResponseBody//支付包扫码结算页面,该处于扫码状态
    public String alipay(HttpServletRequest request, String orderSn, ModelMap modelMap) {
        //将订单支付信息保存到支付表中,该处为未支付状态。支付状态在支付成功后回传,在callbackreturn方法中更新为已支付
        OmsOrder omsOrder = orderService.getOrderByOrderSn(orderSn);//根据订单号获取订单所有信息
        // 保存支付信息业务
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(orderSn);
        paymentInfo.setPaymentStatus("未支付");
        //paymentInfo.setAlipayTradeNo("");
        //paymentInfo.setCallbackContent("");
        //paymentInfo.setCallbackTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setTotalAmount(omsOrder.getTotalAmount());
        paymentInfo.setSubject(omsOrder.getOmsOrderItems().get(0).getProductName());
        //持久化到数据库
        paymentService.save(paymentInfo);

        //跳转到第三方支付平台，支付宝的支付页面
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建api对应的request
        //return_payment_url=http://payment.gmall.com:8090/alipay/callback/return
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);//同步回跳地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址,异步通知服务器更新支付状态

        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderSn);//订单号
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.01);
        map.put("subject", omsOrder.getOmsOrderItems().get(0).getProductName());
        alipayRequest.setBizContent(JSON.toJSONString(map));//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //设置延迟检查订单支付状态的定时任务
        //发送一个用于检查支付状态的延迟队列,PAY_CHECK_QUEUE
        paymentService.sendPaCheckQueue(paymentInfo, 8L);
        System.out.println(form);
        return form;
    }

    @RequestMapping("index")
    @LoginRequired
    //来到订单提交成功页面,并显示订单的相关信息,下一步选择支付方式跳转去扫码支付页面
    public String index(HttpServletRequest request, String orderSn, ModelMap modelMap) {
        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        //根据用户id与订单号联合主键获取订单的详情信息
        OmsOrder omsOrder = orderService.getOrderByUserId(userId, orderSn);
        BigDecimal totalAmount = omsOrder.getTotalAmount();
        modelMap.put("orderSn", orderSn);
        modelMap.put("totalAmount", totalAmount);
        return "index";
    }
}
