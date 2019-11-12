package com.atgg.gmall;

import com.atgg.gmall.cart.config.WebMvcConfiguration;
import com.atgg.gmall.cart.interceptors.AuthInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@ComponentScan(excludeFilters = {
@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,value = WebMvcConfiguration.class),
@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,value = AuthInterceptor.class)
})
@SpringBootApplication
public class GmallPassportApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPassportApplication.class, args);
    }

}
