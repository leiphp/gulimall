package cn.lxtkj.gulimall.search;


import cn.lxtkj.gulimall.search.config.ElasticSearchConfig;
import com.alibaba.fastjson.JSON;
import com.mysql.cj.QueryBindings;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 测试复杂检索
     */
    @Test
    public void searchData() throws IOException {
        //1.创建检索请求
        SearchRequest searchResult = new SearchRequest();
        //指定索引
        searchResult.indices("bank");
        //指定dsl 检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1.1构造查询条件
        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        //1.2安装年龄聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);
        //1.3计算平均值
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        System.out.println(sourceBuilder.toString());

        searchResult.source(sourceBuilder);
        //2.执行检索
        SearchResponse searchResponse = client.search(searchResult,ElasticSearchConfig.COMMON_OPTIONS);
        //3.分析结果
        System.out.println(searchResponse.toString());
        //3.1获取所有查到的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit :searchHits){
//            hit.getIndex();hit.getType();hit.getId();
            String string = hit.getSourceAsString();
            Account account = JSON.parseObject(string,Account.class);
            System.out.println("account:"+account);
        }

        //4. 获取聚合信息
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄："+keyAsString+" ==> "+bucket.getDocCount());
        }
//        Avg ageAvg1 = aggregations.get("ageAvg");
//        System.out.println("平均年龄："+ageAvg1.getValue());

        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资："+balanceAvg1.getValue());



    }

    /**
     * 测试存储数据到es
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");//数据的ID
        User user = new User();
        user.setUserName("zhangshan");
        user.setAge(18);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);//需要保持的内容
        //执行操作
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        //提前有用的响应数据
        System.out.println(index);
    }

    @Data
    class User{
        private String userName;
        private Integer age;
        private String gender;
    }

    @Data
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
