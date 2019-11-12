package com.atgg.gmall.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-21 12:16
 */
@Data
public class Movie {
    private String id;
    private String name;
    private BigDecimal doubanScore;
    private List<ActorList> actorList;
}
