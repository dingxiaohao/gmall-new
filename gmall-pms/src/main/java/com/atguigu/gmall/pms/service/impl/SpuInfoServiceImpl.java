package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.vo.BaseAttrsVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.pms.feign.SmsClient;
import com.atguigu.gmall.pms.service.*;
import dto.SaleDTO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private SmsClient smsClient;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpu(QueryCondition queryCondition, Long catId) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        //等于0是查全站，不等于0就拼接这个条件
        if (catId != 0L){
            wrapper.eq("catalog_id",catId);
        }
        String key = queryCondition.getKey();
        //isEmpty是否为空，是的话就是true，不为空就是false
        //isBlank也是判断为空还有空串更严谨,加上Not就是加!取反的意思,为空的话就是false,不为空就是true
        if (StringUtils.isNotBlank(key)){
            //这里就是or查询，一个查询框条件可以查询匹配多个字段值
            //商品id和商品名称这两个字段在一个查询框就用or,只要输入的查询条件符合这两个字段的值就可以查询出来
            //每一个and后是一个整体，or也是一个整体
            wrapper.and(t -> t.eq("id",key).or().like("spu_name",key));
        }


        //分页，复制就行了,这个Query是把分页对象转为Ipage对象这样才能调用mp的分页方法
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                wrapper
        );

        //pagevo对象返回前端进行展示的分页对象，构造器可以把ipage转为pagevo对象返回前端进行展示
        return new PageVo(page);
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuInfoVO spuInfoVO) {
        /// 1.保存spu相关
        // 1.1. 保存spu基本信息 spu_info
        spuInfoVO.setPublishStatus(1);   //上架状态默认已上架,1上架,0下架
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);
        Long spuId = spuInfoVO.getId(); //获取到主键策略生成的spuId,后续能用上

        // 1.2. 保存spu的描述信息 spu_info_desc
        List<String> spuImages = spuInfoVO.getSpuImages();//获取到传的图片信息
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuId);
        //设置图片信息,join把集合转为字符串每个元素以,分隔
        descEntity.setDecript(StringUtils.join(spuImages,","));
        spuInfoDescService.save(descEntity);

        // 1.3. 保存spu的规格参数信息
        List<BaseAttrsVO> baseAttrs = spuInfoVO.getBaseAttrs();
        //如果这个传的信息不为空的话就进来执行保存其他表
        if (!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> productAttrList =
                    //使用stream流把vo集合转为实体类集合赋值返回
                baseAttrs.stream().map(baseAttrsVO -> {
                  baseAttrsVO.setSpuId(spuId);
                  baseAttrsVO.setAttrSort(0);
                  baseAttrsVO.setQuickShow(0);
                return baseAttrsVO; //必须要写return返回每个元素,一行代码不用写return
            }).collect(Collectors.toList());
            //执行批量保存
            productAttrValueService.saveBatch(productAttrList);
        }

        // 2. 保存sku相关信息
        // 2.1. 保存sku基本信息
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skus)){ //如果这个集合为空那也没有打折啥的了,直接结束方法
            return;
        }
        //因为多个sku,这些操作都在循环里做
        skus.forEach(skuInfoVO -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skuInfoVO,skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            //设置品牌id和三级分类id
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
            skuInfoService.save(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();  //获取主键策略生成的skuId

            // 2.2. 保存sku图片信息
            //获取图片信息,如果传了图片就会保存在这个属性上,没传图片就是null
            List<String> images = skuInfoVO.getImages();
            if (!CollectionUtils.isEmpty(images)){
                String defaultImages = images.get(0);
                List<SkuImagesEntity> skuImagesList = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setSkuId(skuId);        //和默认图片相等是默认图片就是1,不是就是0
                    skuImagesEntity.setDefaultImg(StringUtils.equals(image, defaultImages) ? 1 : 0);
                    skuImagesEntity.setImgSort(0);
                    //这里返回就是集合中的每个元素都是这个对象
                    //stream的map方法就是把一个集合的元素进行转换成什么元素集合返回
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                //批量保存图片,记住图片操作一般都是批量保存多个图片一起传过来的
                //所以保存图片的属性就用list包起来,但是具体的属性类型还是泛型里的类型
                //list包起来的属性就是可以传多个这个属性值,不用list包起来就只能传一个
                //list集合就是一个工具用来保存数据的,不是属性类型,里面泛型才是具体的属性类型
                //接口上也可以直接用list然后泛型里写具体的对象也可以接受多个对象,一个也可以接收
                skuImagesService.saveBatch(skuImagesList);
            }

            SkuImagesEntity one = skuImagesService.getOne(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId).eq("default_img", 1));
            if (one != null){
                skuInfoEntity.setSkuDefaultImg(one.getImgUrl());
                skuInfoService.save(skuInfoEntity);
            }

            // 2.3. 保存sku的规格参数（销售属性）
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            //设置属性名,其他东西都已经前端传数据了
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(saleAttr -> {
                    saleAttr.setSkuId(skuId);
                    saleAttr.setAttrName(attrDao.selectById(saleAttr.getAttrId()).getAttrName());
                    saleAttr.setAttrSort(0);
                });
                //批量新增SkuSale表,多个属性名对应多个属性值
                skuSaleAttrValueService.saveBatch(saleAttrs);
            }
            //TODO 明天完成的部分,再sms微服务实现接口完成下面三个操作
            //TODO 然后再这里使用openfeign远程调用其他微服务接口把参数传过去实现下面的三个操作
            //TODO feign调用需要加注解和依赖还有创建一个feign的映射微服务的接口
            //TODO 可以写这个微服务中的所有接口都可以映射调用,传的参数是DTO对象,微服务间通信的对象
            //TODO DTO对象里的数据也都是前端传过来的参数,然后赋值就行了
            //TODO 明天再看一下feign的最佳实践,一般这个feign的接口就再创建一个接口工程
            //TODO 然后调用者这边引入feign接口的工程,再让调用者自己写的接口继承feign接口就可以了
            //TODO 服务方提供了啥接口我就能全拿到了,具体调哪个看场景

            // 3. 保存营销相关信息，需要远程调用gmall-sms
            // 3.1. 积分优惠
            SaleDTO saleDTO = new SaleDTO();
            BeanUtils.copyProperties(skuInfoVO,saleDTO);
            saleDTO.setSkuId(skuId);
            //注入openfeign接口远程调用其他微服务接口
            smsClient.saveSkuSaleInfo(saleDTO);

            // 3.2. 满减优惠

            // 3.3. 数量折扣
        });


    }

}