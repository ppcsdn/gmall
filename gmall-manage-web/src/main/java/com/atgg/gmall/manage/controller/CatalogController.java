package com.atgg.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.bean.PmsBaseCatalog1;
import com.atgg.gmall.bean.PmsBaseCatalog2;
import com.atgg.gmall.bean.PmsBaseCatalog3;
import com.atgg.gmall.service.CatalogService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-11 15:27
 */

@RestController
@CrossOrigin
public class CatalogController {
    @Reference
    CatalogService catalogService;

    @RequestMapping("getCatalog1")
    public List<PmsBaseCatalog1> getCatalog1() {
       return catalogService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    public List<PmsBaseCatalog2> getCatalog2(String Catalog1Id) {
       return catalogService.getCatalog2(Catalog1Id);
    }

    @RequestMapping("getCatalog3")
    public List<PmsBaseCatalog3> getCatalog3(String Catalog2Id) {
       return catalogService.getCatalog3(Catalog2Id);
    }
}
