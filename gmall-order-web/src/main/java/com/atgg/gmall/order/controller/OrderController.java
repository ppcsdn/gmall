package com.atgg.gmall.order.controller;

import java.util.*;
import java.text.SimpleDateFormat;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.bean.*;
import com.atgg.gmall.cart.annotations.LoginRequired;
import com.atgg.gmall.cart.util.PriceUtil;
import com.atgg.gmall.service.CartService;
import com.atgg.gmall.service.OrderService;
import com.atgg.gmall.service.SkuService;
import com.atgg.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 * @author Pstart
 * @create 2019-10-25 19:37
 */
@Controller
public class OrderController {
    @Reference
    UserService userService;
    @Reference
    CartService cartService;
    @Reference
    OrderService orderService;
    @Reference
    SkuService skuService;

    @LoginRequired//提交订单必须登录(isNeedSuccess = true)
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, String addressId, String tradeCode) {


        UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getAddressById(addressId);

        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        boolean b_tradeCode = orderService.checkTradeCode(userId, tradeCode);
        if (b_tradeCode) {
            OmsOrder omsOrder = new OmsOrder();//订单表的具体信息
            List<OmsCartItem> OmsCartItems = cartService.getCartListByUserId(userId);//订单详情表的具体信息

            String orderSn = "gmallOrder";//订单号唯一的标识
            omsOrder.setStatus("0");//待支付s
            omsOrder.setPayType(2);//支付方式
            SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
            //当前jvm系统时间的毫秒数加上当前时间作唯一标识
            orderSn = orderSn + yyyyMMddHHmmss.format(new Date()) + System.currentTimeMillis();
            omsOrder.setOrderSn(orderSn);

            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            //日历处理类,3天后到货,由商家物流设置
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 3);
            Date time = calendar.getTime();//3天后的具体时间
            omsOrder.setReceiveTime(time);
            omsOrder.setMemberId(userId);
            omsOrder.setMemberUsername(nickName);
            omsOrder.setPayAmount(PriceUtil.getTotalAmout(OmsCartItems));
            omsOrder.setTotalAmount(PriceUtil.getTotalAmout(OmsCartItems));

            //生成订单 获取单点登录用户的商品列表,该列表信息为缓存中购物车数据,将其转换类型成订单数据
            List<String> delCartListIds = new ArrayList<>();//存储已提交订单中的所有购物车Id,提交后将已经提交的订单从购物车中删除
            if (OmsCartItems != null) {
                List<OmsOrderItem> omsOrderItems = new ArrayList<>();
                for (OmsCartItem omsCartItem : OmsCartItems) {
                    if (omsCartItem.getIsChecked().equals("1")) {
                        // 验价
                        boolean b_price = skuService.checkPrice(omsCartItem.getPrice(),omsCartItem.getProductSkuId());
                        // 验库存,java多线程
                        OmsOrderItem omsOrderItem = new OmsOrderItem();

                        omsOrderItem.setProductId(omsCartItem.getProductId());
                        omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                        omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                        omsOrderItem.setProductName(omsCartItem.getProductName());
                        omsOrderItem.setProductPic(omsCartItem.getProductPic());
                        omsOrderItem.setProductPrice(omsCartItem.getPrice());
                        omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                        //点击结算时获取当前被选中的数据根据memberid和product_sku_id联合唯一去数据库找出对应的数据
                        //与redis中数据做一致性校验
                        omsOrderItems.add(omsOrderItem);
                        delCartListIds.add(omsCartItem.getProductSkuId());//添加从购物车中已经提交的订单商品productskuId

//                        if ( cartService.getPriceAndQuantityFromMysql(omsCartItem)){
//                            omsOrderItems.add(omsOrderItem);
//                            delCartListIds.add(omsCartItem.getProductSkuId());//添加从购物车中已经提交的订单商品productskuIds
//                        }else {
//                            return "tradeFail";
//                        }

                    }
                }
                omsOrder.setOmsOrderItems(omsOrderItems);//订单表中set入订单详情表的数据
            }
            orderService.saveOrder(omsOrder);//持久化用户的订单记录保存到数据库
//            cartService.delCartList(delCartListIds, userId);//删除购物车数据,根据用户memberId和商品productskuIds联合唯一删除

            return "redirect:http://payment.gmall.com:8090/index?orderSn="+orderSn;

        } else {
            return "tradeFail";
        }
    }


    @LoginRequired//结算必须登录(isNeedSuccess = true)
    @RequestMapping("toTrade")//去redis缓存中拿
    public String toTrade(HttpServletRequest request, ModelMap map) {
        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        //获取用户收货地址
        UmsMember umsMember = userService.getUserFromCacheById(userId);
        map.put("userAddressList", umsMember.getUmsMemberReceiveAddresses());
        //获取单点登录用户的商品列表,该列表信息为缓存中购物车数据,将其转换类型成订单数据
        List<OmsCartItem> OmsCartItems = cartService.getCartListByUserId(userId);
        if (OmsCartItems != null) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            for (OmsCartItem omsCartItem : OmsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    OmsOrderItem omsOrderItem = new OmsOrderItem();

                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());

                    omsOrderItems.add(omsOrderItem);
                    //生成随机校验码，保存到redis,只由用户选中后点击去结算生成tradeCode
                    String tradeCode = UUID.randomUUID().toString();
                    orderService.saveTradeCode(userId, tradeCode);
                    map.put("tradeCode", tradeCode);
                }
            }
            map.put("orderDetailList", omsOrderItems);

        }
        return "trade";
    }
}
