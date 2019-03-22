package com.atguigu.gmall.search.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.service.SearchService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    JestClient jestClient;
    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        String dsl = getMyDslString(skuLsParam);
        //从elasticsearch中查询数据
        Search build = new Search.Builder(dsl).addIndex("gmall1015").addType("skuLsInfo").build();
        try {
            SearchResult execute = jestClient.execute(build);
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
            //遍历数据并放到集合中
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
               SkuLsInfo source =  hit.source;
                Map<String, List<String>> highlight = hit.highlight;
                if(highlight != null){
                    List<String> skuName = highlight.get("skuName");
                    source.setSkuName(skuName.get(0));
                }
                skuLsInfos.add(source);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsInfos;
    }

    /**
     * 生成dsl语句
     * @param skuLsParam 传入的参数
     * @return 返回dsl语句
     */
    private String getMyDslString(SkuLsParam skuLsParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //获取查询参数
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valuId = skuLsParam.getValueId();
        //过滤
        if (StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder catalog3Id1 = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(catalog3Id1);
        }
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder keyword1 = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(keyword1);
            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field("skuName");
            searchSourceBuilder.highlight(highlightBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);
        String dsl = searchSourceBuilder.toString();
        return dsl;

    }
}
