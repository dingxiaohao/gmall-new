package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Data
//索引库名为goods,分片3
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
public class Goods {
    @Id
    private Long skuId;
    //@Field注解是指明这个属性和索引库的映射关系注解,对应什么类型和是否建立索引库
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;    //默认图片,在商品搜索的时候查询出来全文检索
    //使用什么分词器
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Double)
    private Double price;
    @Field(type = FieldType.Long)
    private Long sale; // 销量

    @Field(type = FieldType.Boolean)
    private Boolean store; // 是否有货,查询wms库存根据skuid

    @Field(type = FieldType.Date)
    private Date createTime; // 新品
    @Field(type = FieldType.Long)
    private Long brandId;
    @Field(type = FieldType.Keyword)
    private String brandName;
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String categoryName;
    @Field(type = FieldType.Nested)
    private List<SearchAttrValue> attrs;

}
