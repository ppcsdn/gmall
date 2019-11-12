package com.atgg.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atgg.gmall.bean.PmsBaseCatalog1;
import com.atgg.gmall.bean.PmsBaseCatalog2;
import com.atgg.gmall.bean.PmsBaseCatalog3;
import com.atgg.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.atgg.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.atgg.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import com.atgg.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-11 20:57
 */

@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;

    @Autowired
    PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;

    @Autowired
    PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return pmsBaseCatalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String Catalog1Id) {
         PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(Catalog1Id);
        return pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String Catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(Catalog2Id);
        return pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);
    }
}
