package com.atguigu.gmall.search.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeign;
import com.atguigu.gmall.search.feign.GmallWmsFeign;
import com.atguigu.gmall.search.pojo.*;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmallwmsinterface.entity.WareSkuEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private GmallPmsFeign pmsFeign;

    @Autowired
    private GmallWmsFeign wmsFeign;

    public SearchResponseVO search(SearchParam searchParam) throws IOException {
        // 构建dsl语句
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);
        SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response);
        SearchResponseVO responseVO = this.parseSearchResult(response);
        responseVO.setPageSize(searchParam.getPageSize());
        responseVO.setPageNum(searchParam.getPageNum());
        return responseVO;
    }

    private SearchResponseVO parseSearchResult(SearchResponse response) throws JsonProcessingException {
        SearchResponseVO responseVO = new SearchResponseVO();
        // 获取总记录数
        SearchHits hits = response.getHits();
        responseVO.setTotal(hits.totalHits);
        // 解析品牌的聚合结果集
        SearchResponseAttrVO brand = new SearchResponseAttrVO();
        brand.setName("品牌");

        // 获取品牌的聚合结果集
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");
        List<String> brandValues = brandIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            // 获取品牌id
            map.put("id", bucket.getKeyAsString());
            // 获取品牌名称：通过子聚合来获取
            Map<String, Aggregation> brandIdSubMap = bucket.getAggregations().asMap();
            ParsedStringTerms brandNameAgg = (ParsedStringTerms)brandIdSubMap.get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            map.put("name", brandName);
            try {
                return OBJECT_MAPPER.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        brand.setValue(brandValues);
        responseVO.setBrand(brand);

        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<String> cateValues = categoryIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            // 获取品牌id
            map.put("id", bucket.getKeyAsString());
            // 获取品牌名称：通过子聚合来获取
            Map<String, Aggregation> categoryIdSubMap = bucket.getAggregations().asMap();
            ParsedStringTerms categoryNameAgg = (ParsedStringTerms)categoryIdSubMap.get("categoryNameAgg");
            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();
            map.put("name", categoryName);
            try {
                return OBJECT_MAPPER.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        SearchResponseAttrVO category = new SearchResponseAttrVO();
        category.setName("分类");
        category.setValue(cateValues);
        responseVO.setCatelog(category);

        // 解析查询列表
        SearchHit[] subHits = hits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        for (SearchHit subHit : subHits) {
            Goods goods = OBJECT_MAPPER.readValue(subHit.getSourceAsString(), new TypeReference<Goods>() {
            });
            goods.setTitle(subHit.getHighlightFields().get("title").getFragments()[0].toString());
            goodsList.add(goods);
        }
        responseVO.setProducts(goodsList);

        // 规格参数
        // 获取嵌套聚合对象
        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");
        // 规格参数id聚合对象
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        List<Terms.Bucket> buckets = (List<Terms.Bucket>)attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            List<SearchResponseAttrVO> searchResponseAttrVOS = buckets.stream().map(bucket -> {
                SearchResponseAttrVO responseAttrVO = new SearchResponseAttrVO();
                // 设置规格参数id
                responseAttrVO.setProductAttributeId(bucket.getKeyAsNumber().longValue());
                // 设置规格参数名
                List<? extends Terms.Bucket> nameBuckets = ((ParsedStringTerms) (bucket.getAggregations().get("attrNameAgg"))).getBuckets();
                responseAttrVO.setName(nameBuckets.get(0).getKeyAsString());
                // 设置规格参数值的列表
                List<? extends Terms.Bucket> valueBuckets = ((ParsedStringTerms) (bucket.getAggregations().get("attrValueAgg"))).getBuckets();
                List<String> values = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                responseAttrVO.setValue(values);
                return responseAttrVO;
            }).collect(Collectors.toList());
            responseVO.setAttrs(searchResponseAttrVOS);
        }


        return responseVO;
    }

    private SearchRequest buildQueryDsl(SearchParam searchParam){

        // 查询关键字
        String keyword = searchParam.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            return null;
        }

        // 查询条件构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1. 构建查询条件和过滤条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1. 构建查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        // 1.2. 构建过滤条件
        // 1.2.1.  构建品牌过滤
        String[] brand = searchParam.getBrand();
        if (brand != null && brand.length != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brand));
        }
        // 1.2.2.  构建分类的过滤
        String[] catelog3 = searchParam.getCatelog3();
        if (catelog3 != null && catelog3.length != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", catelog3));
        }

        // 1.2.3.  构建规格属性嵌套过滤
        String[] props = searchParam.getProps();
        if (props != null && props.length != 0) {
            for (String prop : props) {
                // 以：进行分割，分割后应该是2个元素，1-attrId  2-attrValue(以-分割的字符串)
                String[] split = StringUtils.split(prop, ":");
                if (split == null || split.length != 2) {
                    continue;
                }
                // 以-分割处理出AttrValues
                String[] attrValues = StringUtils.split(split[1], "-");
                // 构建嵌套查询
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                // 构建嵌套查询中子查询
                BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                // 构建子查询中的过滤条件
                subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                subBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 把嵌套查询放入过滤器中
                boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
                boolQueryBuilder.filter(boolQuery);
            }
        }

        // 1.2.4.  价格区间过滤
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
        Integer priceFrom = searchParam.getPriceFrom();
        Integer priceTo = searchParam.getPriceTo();
        if (priceFrom != null) {
            rangeQueryBuilder.gte(priceFrom);
        }
        if (priceTo != null) {
            rangeQueryBuilder.lte(priceTo);
        }
        boolQueryBuilder.filter(rangeQueryBuilder);

        sourceBuilder.query(boolQueryBuilder);

        // 2. 构建分页
        Integer pageNum = searchParam.getPageNum();
        Integer pageSize = searchParam.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        // 3. 构建排序
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] split = StringUtils.split(order, ":");
            if (split != null && split.length == 2) {
                String field = null;
                switch (split[0]) {
                    case "1": field = "sale"; break;
                    case "2": field = "price"; break;
                }
                sourceBuilder.sort(field, StringUtils.equals("asc", split[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        }

        // 4. 构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<em>").postTags("</em>"));

        // 5. 构建聚合
        // 5.1.  品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));
        // 5.2.  分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        // 5.3.  搜索的规格属性聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        System.out.println(sourceBuilder.toString());

        // 结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId", "pic", "title", "price"}, null);

        // 查询参数
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    //同步es索引库的业务方法,让mq监听器调用的,一旦发送消息过来之后监听器就执行对应的业务方法
    public void createIndex(Long spuId) {
        Resp<List<SkuInfoEntity>> skuResp = pmsFeign.querySkuBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntityList = skuResp.getData();
        if (!CollectionUtils.isEmpty(skuInfoEntityList)){
            List<Goods> goodsList = skuInfoEntityList.stream().map(skuInfoEntity -> {
                Goods goods = new Goods();
                goods.setSkuId(skuInfoEntity.getSkuId());
                goods.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                goods.setTitle(skuInfoEntity.getSkuTitle());
                goods.setPrice(skuInfoEntity.getPrice().doubleValue());
                goods.setSale(10l);

                //根据skuid查询是否有库存
                Resp<List<WareSkuEntity>> wareResp = wmsFeign.queryListBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareList = wareResp.getData();
                //设置是否有库存,通过stream流判断元素
                goods.setStore(wareList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                //调用远程接口根据spuId查询具体的一条记录,spuInfo
                Resp<SpuInfoEntity> spuInfoResp = pmsFeign.querySpuById(spuId);
                //获取到记录对应的实体类,实体类对应的具体一条记录字段数据
                SpuInfoEntity spuInfoEntity = spuInfoResp.getData();
                goods.setCreateTime(spuInfoEntity.getCreateTime()); //设置时间
                //根据品牌id查询品牌名
                Resp<BrandEntity> brandResp = pmsFeign.queryBrandById(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandResp.getData();
                if (brandEntity != null) {
                    goods.setBrandId(skuInfoEntity.getBrandId());
                    goods.setBrandName(brandEntity.getName());
                }

                //根据分类id查询分类名,getData是因为后端返回数据的时候就是保存到data里了,这里get获取就行了
                Resp<CategoryEntity> categoryResp = pmsFeign.queryCategoryById(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryResp.getData();
                if (categoryEntity != null) {
                    goods.setCategoryName(categoryEntity.getName());
                    goods.setCategoryId(skuInfoEntity.getCatalogId());
                }
                //拿到需要检索查询的属性值,product
                Resp<List<ProductAttrValueEntity>> attrsBySpuIdResp = pmsFeign.querySearchAttrsBySpuId(skuInfoEntity.getSpuId());
                List<ProductAttrValueEntity> attrValueEntities = attrsBySpuIdResp.getData();
                List<SearchAttrValue> searchAttrList = attrValueEntities.stream().map(productAttrValueEntity -> {
                    SearchAttrValue searchAttrValue = new SearchAttrValue();
                    searchAttrValue.setAttrId(productAttrValueEntity.getAttrId());
                    searchAttrValue.setAttrName(productAttrValueEntity.getAttrName());
                    searchAttrValue.setAttrValue(productAttrValueEntity.getAttrValue());
                    return searchAttrValue;
                }).collect(Collectors.toList());
                goods.setAttrs(searchAttrList);
                return goods;
            }).collect(Collectors.toList());

            //执行es的保存
            repository.saveAll(goodsList);
        }
    }
}
