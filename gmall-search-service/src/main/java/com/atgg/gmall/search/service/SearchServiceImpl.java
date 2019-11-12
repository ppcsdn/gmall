package com.atgg.gmall.search.service;
import	java.util.ArrayList;

import com.alibaba.dubbo.config.annotation.Service;
import com.atgg.gmall.bean.PmsSearchParam;
import com.atgg.gmall.bean.PmsSearchSkuInfo;
import com.atgg.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Pstart
 * @create 2019-10-21 16:03
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    JestClient jestClient;

    public String getDsl(PmsSearchParam pmsSearchParam) {
        //构建资源查询的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();

        //bool/term/filter 根据条件过滤查询es中的内容并显示到前端页面
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if (StringUtils.isNotBlank(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //关键字查询 注意使用匹配match/must
        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder termQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(termQueryBuilder);
            //高亮显示的条件设置
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlight(highlightBuilder);

        }
        //单个的平台属性值的id
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        SearchSourceBuilder query = searchSourceBuilder.query(boolQueryBuilder);
        query.from(0);
        query.size(20);

        String dsl = query.toString();

        return dsl;
    }

    @Override
    public List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        //表/库/主键(这里就是条件查询dsl语句)
        String dsl = getDsl(pmsSearchParam);
        System.out.println(dsl);
        Search search = new Search.Builder(dsl).addIndex("gmallsku").addType("pmsSearchSkuInfo").build();

        try {//获得结果返回
            SearchResult searchResult = jestClient.execute(search);
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
                //处理高亮显示
                Map<String, List<String>> highlight = hit.highlight;
                if (highlight != null&&highlight.size()>0) {
                    List<String> list = highlight.get("skuName");
                    String skuName = list.get(0);
                    pmsSearchSkuInfo.setSkuName(skuName);
                }
                pmsSearchSkuInfos.add(pmsSearchSkuInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pmsSearchSkuInfos;
    }
}
