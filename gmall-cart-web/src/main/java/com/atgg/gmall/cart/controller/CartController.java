package com.atgg.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.bean.OmsCartItem;
import com.atgg.gmall.bean.PmsSkuInfo;
import com.atgg.gmall.cart.annotations.LoginRequired;
import com.atgg.gmall.cart.util.CookieUtil;
import com.atgg.gmall.cart.util.PriceUtil;
import com.atgg.gmall.service.CartService;
import com.atgg.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-23 11:07
 */
@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;
    String userId = "1";

    @RequestMapping("checkCart")//检查购物车返回给cartListInner内嵌页面
    @LoginRequired(isNeedSuccess = false)
    public String checkCart(HttpServletRequest request, HttpServletResponse response, OmsCartItem omsCartItem, ModelMap map) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        if (StringUtils.isNotBlank(userId)) {//用户登录查缓存
            omsCartItem.setMemberId(userId);//设置用户为登录状态
            cartService.updateCartByUserId(omsCartItem);//根据memberId与productSkuId联合唯一更新数据库并同步缓存
            omsCartItems = cartService.getCartListByUserId(userId); //去缓存中取数据并回显,结算时需要用到
        } else {//用户未登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);//将得到的cookie数据转换
                for (OmsCartItem cartItem : omsCartItems) {//遍历cookie中商品与用户选中的sku进行对比更新cookie
                    if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {//改变cookie中的选中状态更新到cookie
                        cartItem.setIsChecked(omsCartItem.getIsChecked());
                    }
                }
                //覆盖cookie
                CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 24, true);
            }
        }
        if (omsCartItems != null && omsCartItems.size() > 0) {
            map.put("cartList", omsCartItems);
            map.put("totalAmount", PriceUtil.getTotalAmout(omsCartItems));

        }
        return "cartListInner";
    }


    @RequestMapping("addToCart")
    @LoginRequired(isNeedSuccess = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response, OmsCartItem omsCartItem) {

        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoById(omsCartItem.getProductSkuId());//查询商品信息回显到购物车列表
        omsCartItem.setIsChecked("1");//默认商品选中
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {//登录状态
            omsCartItem.setMemberId(userId);
            omsCartItem.setMemberNickname("用户昵称");
            //根据memberid=1与商品的sku_id联合唯一判断是否存在于购物车
            OmsCartItem omsCartItemFromDb = cartService.isCartExits(userId, omsCartItem);
            if (omsCartItemFromDb != null) {//更新和缓存同步
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                omsCartItemFromDb.setPrice(omsCartItemFromDb.getPrice().multiply(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            } else {//新增和缓存同步
                cartService.addCart(omsCartItem);
            }
        } else {//非登录状态,将用户数据存入omsCartItems,操作omsCartItems,设置一个omsCartItems的集合
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);//获取omsCartItems中的值
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                boolean b = if_new_cart(omsCartItems, omsCartItem);
                if (b) {//为true表示omsCartItems中不存在该信息,即添加,否则重复即叠加数量与价格
                    omsCartItems.add(omsCartItem);
                } else {//
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                            cartItem.setPrice(cartItem.getPrice().multiply(omsCartItem.getPrice()));
                        }
                    }
                }
            } else {//omsCartItems为空直接添加
                omsCartItems.add(omsCartItem);
            }//将最终得到的omsCartItems结果覆盖
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 24, true);
        }

        return "redirect:/success.html";
    }


    @RequestMapping("cartList")
    @LoginRequired(isNeedSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, OmsCartItem omsCartItem, ModelMap map) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {//用户登录查缓存
            omsCartItems = cartService.cartList(userId);
        } else {//用户未登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }

        if (omsCartItems != null && omsCartItems.size() > 0) {
            map.put("cartList", omsCartItems);
            map.put("totalAmount", PriceUtil.getTotalAmout(omsCartItems));//价格工具类，购物车被选中时计算价格和总价格
        }
        return "cartList";
    }

    private boolean if_new_cart(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = true;//默认true表示购物车集合中不存在用户新点入的商品
        for (OmsCartItem cartItem : omsCartItems) {//遍历购物车集合存在则返回false,即重复添加,将数目和价格叠加就行
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                b = false;
            }
        }
        return b;
    }


}
