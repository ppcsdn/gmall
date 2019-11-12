package com.atgg.gmall.cart.controller;

import java.math.BigDecimal;

/**
 * @author Pstart
 * @create 2019-10-24 16:52
 */
public class PriceTest {
    public static void main(String[] args) {
        //项目中有关monney的计算必须使用bigdecimal

        BigDecimal bigDecimal = new BigDecimal("0.01");
        BigDecimal bigDecimal1 = new BigDecimal(0.01d);
        BigDecimal bigDecimal2 = new BigDecimal(0.01f);
        System.out.println(bigDecimal);
        System.out.println(bigDecimal1);
        System.out.println(bigDecimal2);
        int i = bigDecimal.compareTo(bigDecimal2);
        System.out.println(i);
        BigDecimal bigDecimal3 = new BigDecimal("6.19999");
        BigDecimal bigDecimal4 = new BigDecimal("7");
        bigDecimal3.add(bigDecimal4);
        bigDecimal4.subtract(bigDecimal3);
        bigDecimal4.multiply(bigDecimal3);
        //精确到小数点后几位
        BigDecimal divide = bigDecimal3.divide(bigDecimal4,3,BigDecimal.ROUND_HALF_DOWN);
        BigDecimal bigDecimal5 = bigDecimal3.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(divide);
        System.out.println(bigDecimal5);
    }
}
