package com.liujie.gmall.list.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.liujie.gmall.*;
import com.liujie.gmall.config.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

   /* ListService listService;*/



    public static final String ES_INDEX = "gmall";
    public static final String ES_TYPE = "SkuInfo";



    /*private void sendSkuToList(SkuInfo skuInfo){
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);
    }*/


    public void saveSkuInfo(SkuLsInfo skuLsInfo){
        Index build = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).build();

        try {
             jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    public SkuLsResult searchSkuinfoList(SkuLsParams skuLsParams){
        String query = makeQueryStringForSearch(skuLsParams);
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult=null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams, searchResult);
        System.err.println("skuLs---------------------------"+skuLsResult);
        return skuLsResult;
    }



    public SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult){
        SkuLsResult skuLsResult = new SkuLsResult();
        System.err.println("skuLsResult"+skuLsResult);
        List<SkuLsInfo> list = new ArrayList<>(skuLsParams.getPageSize());
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            if (hit.highlight!=null&&hit.highlight.size()>0) {
                List<String> list1 = hit.highlight.get("skuName");
                String s = list1.get(0);
                skuLsInfo.setSkuName(s);

            }
            list.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(list);
        skuLsResult.setTotal(searchResult.getTotal());

        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation termsAggregation = aggregations.getTermsAggregation("groupby_valueId");
        List keyList = new ArrayList();
        System.err.println("kong?"+termsAggregation==null?"1":"2");
        if(termsAggregation!=null) {
            List<TermsAggregation.Entry> buckets = termsAggregation.getBuckets();


            for (TermsAggregation.Entry bucket : buckets) {
                String key = bucket.getKey();
                keyList.add(key);
            }

            skuLsResult.setAttrValueIdList(keyList);
        }
        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if (skuLsParams.getCatalog3Id()!=null){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }





        if(skuLsParams.getKeyword()!=null){

            //bool\must
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
            //must
            boolQueryBuilder.must(matchQueryBuilder);

            //highlight
            HighlightBuilder highlightBuilder= new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field(skuLsParams.getKeyword());

            //h
            searchSourceBuilder.highlight(highlightBuilder);


        }
        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){

            List<String> list = Arrays.asList(skuLsParams.getValueId());
            //filter
            TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId",list);
            //bool\filter

            boolQueryBuilder.filter(termsQueryBuilder);
        }
        //聚合
        TermsBuilder groupby_valueId = AggregationBuilders.terms("groupby_valueId").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_valueId);

        //查询
        searchSourceBuilder.query(boolQueryBuilder);

        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        String search = searchSourceBuilder.toString();
        System.err.println("search:"+search);


        return search;
    }
   /* @Test
    public void testSearch(){
        SkuLsParams skuLsParams = new SkuLsParams();
        skuLsParams.setCatalog3Id("61");
        skuLsParams.setKeyword("苹果");

        skuLsParams.setValueId(new String[]{"1","3"});
        makeQueryStringForSearch(skuLsParams);
    }*/

   public  void updateScore(String skuId,Double hotScore){
       String update = "{\n" +
               "  \"doc\": {\n" +
               "    \"hotScore\":\""+hotScore+"\"\n" +
               "  }\n" +
               "}";
       Update build = new Update.Builder(update).index(ES_INDEX).type(ES_TYPE).id(skuId).build();
       try {
           jestClient.execute(build);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   public void incrHotScore(String skuId){
       Jedis jedis = redisUtil.getJedis();
       Double hotScore = jedis.zincrby("hotScore", 1, skuId);
       if(hotScore%10==0){
           updateScore(skuId, hotScore);
       }
   }

}
