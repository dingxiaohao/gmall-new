package com.atguigu.gmall.search;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeign;
import com.atguigu.gmall.search.feign.GmallWmsFeign;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmallwmsinterface.entity.WareSkuEntity;
import com.atguigu.gmallwmsinterface.feign.GmallWmsApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private GmallPmsFeign pmsFeign;

    @Autowired
    private GmallWmsFeign wmsFeign;

    @Test
    void contextLoads() {
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
    }

    @Test
    void importData() {

        Long pageNum = 1l;
        Long pageSize = 100l;

        do {
            // 分页查询spu
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> listResp = pmsFeign.querySpuByPage(queryCondition);
            List<SpuInfoEntity> spuInfoEntityList = listResp.getData();
            //判断spulist是否为空
            if (CollectionUtils.isEmpty(spuInfoEntityList)){
                return;
            }
           //遍历spulist导入到es
            spuInfoEntityList.forEach(spuInfoEntity -> {
                Resp<List<SkuInfoEntity>> skuResp = pmsFeign.querySkuBySpuId(spuInfoEntity.getId());
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
                        goods.setStore(wareList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() >0));

                        goods.setCreateTime(spuInfoEntity.getCreateTime());
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

            });

            pageSize = (long) spuInfoEntityList.size();
            pageNum++;
        } while (pageSize == 100);

    }
}
