package com.atgg.gmall.search;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.bean.Movie;
import com.atgg.gmall.bean.PmsSearchSkuInfo;
import com.atgg.gmall.bean.PmsSkuInfo;
import com.atgg.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {
    @Autowired
    JestClient jestClient;
    @Reference
    SkuService skuService;

//    @Test
//    public void contextLoads()throws IOException{
//        //导入数据
//        //增加
//        List<PmsSkuInfo> pmsSkuInfos=skuService.getSkuInfo();
//        //转化成search的sku对象
//        List<PmsSearchSkuInfo>pmsSearchSkuInfos=new ArrayList<>();
//        for(PmsSkuInfo pmsSkuInfo:pmsSkuInfos){
//            PmsSearchSkuInfo pmsSearchSkuInfo=new PmsSearchSkuInfo();
//            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
//            //主键配型
//            pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));
//            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
//        }
//
//        for(PmsSearchSkuInfo pmsSkuInfo:pmsSearchSkuInfos){
//            Index index=new Index.Builder(pmsSkuInfo).index("gmallsku").type("pmsSearchSkuInfo").id(pmsSkuInfo.getId()+"").build();
//            jestClient.execute(index);
//        }
//
//    }

    @Test
    public void contextLoads() throws IOException {
        List<PmsSkuInfo> pmsSkuInfo = skuService.getSkuInfo();//拿到所有数据了，将数据put进es中
        PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
        for (PmsSkuInfo skuInfo : pmsSkuInfo) {
            pmsSearchSkuInfo.setId(Long.parseLong(skuInfo.getId()));
            BeanUtils.copyProperties(skuInfo, pmsSearchSkuInfo);
            //库/表/主键
            Index index = new Index.Builder(pmsSearchSkuInfo).index("gmallsku").type("pmsSearchSkuInfo").id(skuInfo.getId()+"").build();
            jestClient.execute(index);
        }
    }

    public void testQuery() throws IOException {
        String dsl = getDsl();
        Search search = new Search.Builder(dsl).addIndex("movie_index").addType("movie").build();
        SearchResult searchResult = jestClient.execute(search);

        List<Movie> movies = new ArrayList<>();

        List<SearchResult.Hit<Movie, Void>> hits = searchResult.getHits(Movie.class);
        for (SearchResult.Hit<Movie, Void> hit : hits) {
            Movie source = hit.source;
            System.out.println(source.getName() + "::" + source.getActorList());
            movies.add(source);
        }
    }

    public void common() throws IOException {
        // 通过http的rest风格的请求发送dsl的json语句
        // 增加
        Index index = new Index.Builder(null).index("").type("").id("").build();
        // 查询
        Search search = new Search.Builder(null).addIndex("").addType("").build();
        // 删除
        Delete delete = new Delete.Builder(null).build();
        // 修改
        Update update = new Update.Builder(null).build();

        jestClient.execute(index);
    }

    //dsl查询语句
    public static String getDsl() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //过滤条件
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("actorList.id", "2");
        boolQueryBuilder.filter(termQueryBuilder);
        //匹配结果
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("name", "行动");
        boolQueryBuilder.filter(matchQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);

        System.out.println(searchSourceBuilder.toString());
        return searchSourceBuilder.toString();
    }


}
