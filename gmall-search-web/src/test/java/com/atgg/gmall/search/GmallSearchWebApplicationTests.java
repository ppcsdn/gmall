package com.atgg.gmall.search;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.bean.PmsBaseCatalog1;
import com.atgg.gmall.bean.PmsBaseCatalog2;
import com.atgg.gmall.bean.PmsBaseCatalog3;
import com.atgg.gmall.service.CatalogService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchWebApplicationTests {
    @Reference
    CatalogService catalogService;
    @Test
    public void contextLoads() throws IOException {
        List<PmsBaseCatalog1> catalog1 = catalogService.getCatalog1();
        for (PmsBaseCatalog1 pmsBaseCatalog1 : catalog1) {
            List<PmsBaseCatalog2> catalog2 = catalogService.getCatalog2(pmsBaseCatalog1.getId());
            for (PmsBaseCatalog2 pmsBaseCatalog2 : catalog2) {
                List<PmsBaseCatalog3> catalog3 = catalogService.getCatalog3(pmsBaseCatalog2.getId());
                pmsBaseCatalog2.setCatalog3List(catalog3);
            }
            pmsBaseCatalog1.setCatalog2s(catalog2);
        }


        File file = new File("d:/catalog.json");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(JSON.toJSONString(catalog1).getBytes());

    }

}
