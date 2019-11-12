package com.atgg.gmall.service;

import com.atgg.gmall.bean.PmsSearchParam;
import com.atgg.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-21 16:00
 */
public interface SearchService {

    List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam);
}
