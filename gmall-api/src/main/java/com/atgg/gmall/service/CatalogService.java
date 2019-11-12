package com.atgg.gmall.service;

import com.atgg.gmall.bean.PmsBaseCatalog1;
import com.atgg.gmall.bean.PmsBaseCatalog2;
import com.atgg.gmall.bean.PmsBaseCatalog3;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-11 18:34
 */
public interface CatalogService {

    public List<PmsBaseCatalog1> getCatalog1();

    public List<PmsBaseCatalog2> getCatalog2(String Catalog1Id);

    public List<PmsBaseCatalog3> getCatalog3(String Catalog2Id);
}

