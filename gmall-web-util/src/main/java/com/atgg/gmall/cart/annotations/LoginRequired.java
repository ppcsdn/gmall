package com.atgg.gmall.cart.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    //对于是否必须成功，默认true。false对于购物车的用户不登录
    boolean isNeedSuccess() default true;
}
